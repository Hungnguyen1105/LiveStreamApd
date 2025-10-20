package com.example.livestream_apd.presentation.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> chatSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomIdFromSession(session);
        if (roomId != null) {
            chatSessions.put(session.getId(), session);
            sessionRoomMap.put(session.getId(), roomId);
            log.info("WebSocket connection established for chat room: {} session: {}", roomId, session.getId());
            
            // Send welcome message
            sendMessage(session, Map.of(
                "type", "connection_established",
                "roomId", roomId,
                "message", "Kết nối chat thành công"
            ));
        } else {
            log.warn("No room ID found in WebSocket session, closing connection");
            session.close();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMessage) {
            try {
                Map<String, Object> payload = objectMapper.readValue(textMessage.getPayload(), Map.class);
                String messageType = (String) payload.get("type");
                String roomId = sessionRoomMap.get(session.getId());

                log.info("Received WebSocket message: {} in room: {}", messageType, roomId);

                switch (messageType) {
                    case "chat_message":
                        handleChatMessage(session, payload, roomId);
                        break;
                    case "typing_start":
                        handleTypingIndicator(session, payload, roomId, true);
                        break;
                    case "typing_stop":
                        handleTypingIndicator(session, payload, roomId, false);
                        break;
                    case "join_room":
                        handleJoinRoom(session, payload);
                        break;
                    case "leave_room":
                        handleLeaveRoom(session);
                        break;
                    default:
                        log.warn("Unknown message type: {}", messageType);
                }
            } catch (Exception e) {
                log.error("Error handling WebSocket message", e);
                sendError(session, "Error processing message");
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed for session: {} with status: {}", session.getId(), closeStatus);
        removeSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleChatMessage(WebSocketSession session, Map<String, Object> payload, String roomId) {
        try {
            // Broadcast message to all sessions in the same room
            Map<String, Object> broadcastMessage = Map.of(
                "type", "new_message",
                "roomId", roomId,
                "content", payload.get("content"),
                "userId", sessionUserMap.get(session.getId()),
                "timestamp", System.currentTimeMillis()
            );

            broadcastToRoom(roomId, broadcastMessage, session.getId());
        } catch (Exception e) {
            log.error("Error handling chat message", e);
        }
    }

    private void handleTypingIndicator(WebSocketSession session, Map<String, Object> payload, String roomId, boolean isTyping) {
        try {
            Long userId = sessionUserMap.get(session.getId());
            if (userId != null) {
                Map<String, Object> typingMessage = Map.of(
                    "type", isTyping ? "user_typing" : "user_stopped_typing",
                    "roomId", roomId,
                    "userId", userId,
                    "timestamp", System.currentTimeMillis()
                );

                broadcastToRoom(roomId, typingMessage, session.getId());
            }
        } catch (Exception e) {
            log.error("Error handling typing indicator", e);
        }
    }

    private void handleJoinRoom(WebSocketSession session, Map<String, Object> payload) {
        try {
            String newRoomId = (String) payload.get("roomId");
            Long userId = ((Number) payload.get("userId")).longValue();
            
            if (newRoomId != null) {
                sessionRoomMap.put(session.getId(), newRoomId);
                sessionUserMap.put(session.getId(), userId);
                
                // Notify others in the room
                Map<String, Object> joinMessage = Map.of(
                    "type", "user_joined",
                    "roomId", newRoomId,
                    "userId", userId,
                    "timestamp", System.currentTimeMillis()
                );

                broadcastToRoom(newRoomId, joinMessage, session.getId());
                
                sendMessage(session, Map.of(
                    "type", "room_joined",
                    "roomId", newRoomId,
                    "message", "Đã tham gia phòng chat"
                ));
            }
        } catch (Exception e) {
            log.error("Error handling join room", e);
        }
    }

    private void handleLeaveRoom(WebSocketSession session) {
        try {
            String roomId = sessionRoomMap.get(session.getId());
            Long userId = sessionUserMap.get(session.getId());
            
            if (roomId != null && userId != null) {
                Map<String, Object> leaveMessage = Map.of(
                    "type", "user_left",
                    "roomId", roomId,
                    "userId", userId,
                    "timestamp", System.currentTimeMillis()
                );

                broadcastToRoom(roomId, leaveMessage, session.getId());
            }
        } catch (Exception e) {
            log.error("Error handling leave room", e);
        }
    }

    private void broadcastToRoom(String roomId, Map<String, Object> message, String excludeSessionId) {
        chatSessions.entrySet().parallelStream()
            .filter(entry -> !entry.getKey().equals(excludeSessionId))
            .filter(entry -> roomId.equals(sessionRoomMap.get(entry.getKey())))
            .forEach(entry -> {
                try {
                    sendMessage(entry.getValue(), message);
                } catch (Exception e) {
                    log.error("Error broadcasting to session: {}", entry.getKey(), e);
                }
            });
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            log.error("Error sending WebSocket message to session: {}", session.getId(), e);
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        Map<String, Object> error = Map.of(
            "type", "error",
            "message", errorMessage,
            "timestamp", System.currentTimeMillis()
        );
        sendMessage(session, error);
    }

    private void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        String roomId = sessionRoomMap.get(sessionId);
        Long userId = sessionUserMap.get(sessionId);
        
        chatSessions.remove(sessionId);
        sessionRoomMap.remove(sessionId);
        sessionUserMap.remove(sessionId);
        
        // Notify others that user left
        if (roomId != null && userId != null) {
            Map<String, Object> leaveMessage = Map.of(
                "type", "user_disconnected",
                "roomId", roomId,
                "userId", userId,
                "timestamp", System.currentTimeMillis()
            );
            broadcastToRoom(roomId, leaveMessage, sessionId);
        }
    }

    private String getRoomIdFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        // Extract room ID from path like /ws/chat/{roomId}
        if (path != null && path.startsWith("/ws/chat/")) {
            return path.substring("/ws/chat/".length());
        }
        return null;
    }

    // Public methods for external use
    public void sendMessageToRoom(String roomId, Map<String, Object> message) {
        broadcastToRoom(roomId, message, null);
    }

    public void sendMessageToUser(Long userId, Map<String, Object> message) {
        sessionUserMap.entrySet().stream()
            .filter(entry -> userId.equals(entry.getValue()))
            .forEach(entry -> {
                WebSocketSession session = chatSessions.get(entry.getKey());
                if (session != null) {
                    sendMessage(session, message);
                }
            });
    }
}
