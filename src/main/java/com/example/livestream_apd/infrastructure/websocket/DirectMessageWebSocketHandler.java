package com.example.livestream_apd.infrastructure.websocket;

import com.example.livestream_apd.application.dto.request.SendDirectMessageRequest;
import com.example.livestream_apd.application.dto.response.DirectMessageResponse;
import com.example.livestream_apd.domain.service.DirectMessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class DirectMessageWebSocketHandler extends TextWebSocketHandler {

    private final DirectMessageService messageService;
    private final ObjectMapper objectMapper;

    // Store active sessions: conversationId -> Map<userId, session>
    private final Map<Long, Map<Long, WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();
    
    // Store user sessions: userId -> Map<conversationId, session>
    private final Map<Long, Map<Long, WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Direct message WebSocket connection established: {}", session.getId());
        
        Long userId = getUserIdFromSession(session);
        Long conversationId = getConversationIdFromSession(session);
        
        if (userId != null && conversationId != null) {
            addSessionToConversation(conversationId, userId, session);
            sendConnectionConfirmation(session, userId, conversationId);
        } else {
            log.warn("Missing userId or conversationId in session: {}", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("Missing userId or conversationId"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Long userId = getUserIdFromSession(session);
            Long conversationId = getConversationIdFromSession(session);
            
            if (userId == null || conversationId == null) {
                sendErrorMessage(session, "Invalid session");
                return;
            }

            JsonNode messageNode = objectMapper.readTree(message.getPayload());
            String type = messageNode.get("type").asText();

            switch (type) {
                case "SEND_MESSAGE":
                    handleSendMessage(session, userId, conversationId, messageNode);
                    break;
                case "TYPING_START":
                    handleTypingStart(userId, conversationId);
                    break;
                case "TYPING_STOP":
                    handleTypingStop(userId, conversationId);
                    break;
                case "MARK_AS_READ":
                    handleMarkAsRead(userId, conversationId, messageNode);
                    break;
                case "PING":
                    handlePing(session);
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
                    sendErrorMessage(session, "Unknown message type");
            }

        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
            sendErrorMessage(session, "Internal server error");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Direct message WebSocket connection closed: {} - {}", session.getId(), status);
        removeSession(session);
    }

    private void handleSendMessage(WebSocketSession session, Long userId, Long conversationId, JsonNode messageNode) {
        try {
            SendDirectMessageRequest request = objectMapper.treeToValue(messageNode.get("data"), SendDirectMessageRequest.class);
            request.setConversationId(conversationId);
            
            var response = messageService.sendMessage(userId, request);
            
            if (response.getSuccess()) {
                // Broadcast message to all participants in the conversation
                broadcastToConversation(conversationId, Map.of(
                    "type", "NEW_MESSAGE",
                    "data", response.getData()
                ), userId);
                
                // Send confirmation to sender
                sendMessage(session, Map.of(
                    "type", "MESSAGE_SENT",
                    "data", response.getData()
                ));
            } else {
                sendErrorMessage(session, response.getError());
            }
            
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            sendErrorMessage(session, "Failed to send message");
        }
    }

    private void handleTypingStart(Long userId, Long conversationId) {
        broadcastToConversation(conversationId, Map.of(
            "type", "TYPING_START",
            "data", Map.of("userId", userId)
        ), userId);
    }

    private void handleTypingStop(Long userId, Long conversationId) {
        broadcastToConversation(conversationId, Map.of(
            "type", "TYPING_STOP",
            "data", Map.of("userId", userId)
        ), userId);
    }

    private void handleMarkAsRead(Long userId, Long conversationId, JsonNode messageNode) {
        try {
            Long messageId = messageNode.get("data").get("messageId").asLong();
            var response = messageService.markMessageAsRead(messageId, userId);
            
            if (response.getSuccess()) {
                broadcastToConversation(conversationId, Map.of(
                    "type", "MESSAGE_READ",
                    "data", Map.of(
                        "messageId", messageId,
                        "readBy", userId
                    )
                ), null); // Send to all participants including sender
            }
            
        } catch (Exception e) {
            log.error("Error marking message as read: {}", e.getMessage(), e);
        }
    }

    private void handlePing(WebSocketSession session) {
        sendMessage(session, Map.of("type", "PONG"));
    }

    private void addSessionToConversation(Long conversationId, Long userId, WebSocketSession session) {
        conversationSessions.computeIfAbsent(conversationId, k -> new ConcurrentHashMap<>())
                .put(userId, session);
        
        userSessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(conversationId, session);
        
        log.debug("Added session for user {} in conversation {}", userId, conversationId);
    }

    private void removeSession(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        Long conversationId = getConversationIdFromSession(session);
        
        if (userId != null && conversationId != null) {
            // Remove from conversation sessions
            Map<Long, WebSocketSession> convSessions = conversationSessions.get(conversationId);
            if (convSessions != null) {
                convSessions.remove(userId);
                if (convSessions.isEmpty()) {
                    conversationSessions.remove(conversationId);
                }
            }
            
            // Remove from user sessions
            Map<Long, WebSocketSession> userConvSessions = userSessions.get(userId);
            if (userConvSessions != null) {
                userConvSessions.remove(conversationId);
                if (userConvSessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            
            log.debug("Removed session for user {} in conversation {}", userId, conversationId);
        }
    }

    private void broadcastToConversation(Long conversationId, Map<String, Object> message, Long excludeUserId) {
        Map<Long, WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions != null) {
            sessions.forEach((userId, session) -> {
                if (excludeUserId == null || !userId.equals(excludeUserId)) {
                    sendMessage(session, message);
                }
            });
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                String messageJson = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(messageJson));
            }
        } catch (IOException e) {
            log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException closeException) {
                log.error("Error closing session: {}", closeException.getMessage());
            }
        }
    }

    private void sendErrorMessage(WebSocketSession session, String error) {
        sendMessage(session, Map.of(
            "type", "ERROR",
            "data", Map.of("error", error)
        ));
    }

    private void sendConnectionConfirmation(WebSocketSession session, Long userId, Long conversationId) {
        sendMessage(session, Map.of(
            "type", "CONNECTION_ESTABLISHED",
            "data", Map.of(
                "userId", userId,
                "conversationId", conversationId,
                "timestamp", System.currentTimeMillis()
            )
        ));
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            Object userIdAttr = session.getAttributes().get("userId");
            if (userIdAttr != null) {
                return Long.parseLong(userIdAttr.toString());
            }
        } catch (Exception e) {
            log.error("Error getting userId from session: {}", e.getMessage());
        }
        return null;
    }

    private Long getConversationIdFromSession(WebSocketSession session) {
        try {
            Object conversationIdAttr = session.getAttributes().get("conversationId");
            if (conversationIdAttr != null) {
                return Long.parseLong(conversationIdAttr.toString());
            }
        } catch (Exception e) {
            log.error("Error getting conversationId from session: {}", e.getMessage());
        }
        return null;
    }

    // Public methods for external use (e.g., from REST endpoints)
    
    public void notifyNewMessage(Long conversationId, DirectMessageResponse message) {
        broadcastToConversation(conversationId, Map.of(
            "type", "NEW_MESSAGE",
            "data", message
        ), null);
    }

    public void notifyMessageUpdated(Long conversationId, DirectMessageResponse message) {
        broadcastToConversation(conversationId, Map.of(
            "type", "MESSAGE_UPDATED",
            "data", message
        ), null);
    }

    public void notifyMessageDeleted(Long conversationId, Long messageId) {
        broadcastToConversation(conversationId, Map.of(
            "type", "MESSAGE_DELETED",
            "data", Map.of("messageId", messageId)
        ), null);
    }

    public boolean isUserOnline(Long userId, Long conversationId) {
        Map<Long, WebSocketSession> userConvSessions = userSessions.get(userId);
        if (userConvSessions != null) {
            WebSocketSession session = userConvSessions.get(conversationId);
            return session != null && session.isOpen();
        }
        return false;
    }

    public int getActiveUsersInConversation(Long conversationId) {
        Map<Long, WebSocketSession> sessions = conversationSessions.get(conversationId);
        return sessions != null ? (int) sessions.values().stream()
                .filter(WebSocketSession::isOpen)
                .count() : 0;
    }
}
