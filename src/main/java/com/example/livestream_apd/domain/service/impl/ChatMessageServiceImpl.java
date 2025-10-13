package com.example.livestream_apd.domain.service.impl;

import com.example.livestream_apd.application.dto.request.EditMessageRequest;
import com.example.livestream_apd.application.dto.request.ReactToMessageRequest;
import com.example.livestream_apd.application.dto.request.SendMessageRequest;
import com.example.livestream_apd.application.dto.request.TypingIndicatorRequest;
import com.example.livestream_apd.application.dto.response.ChatMessageResponse;
import com.example.livestream_apd.application.dto.response.UserSummaryResponse;
import com.example.livestream_apd.domain.entity.ChatMessage;
import com.example.livestream_apd.domain.entity.ChatRoom;
import com.example.livestream_apd.domain.entity.Role;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.ChatMessageRepository;
import com.example.livestream_apd.domain.repository.ChatRoomRepository;
import com.example.livestream_apd.domain.service.ChatMessageService;
import com.example.livestream_apd.utils.exceptions.ResourceForbiddenException;
import com.example.livestream_apd.utils.exceptions.ResourceNotFoundException;
import com.example.livestream_apd.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(SendMessageRequest request, User currentUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", request.getChatRoomId()));

        if (!chatRoom.getIsActive()) {
            throw new ResourceForbiddenException("Phòng chat không hoạt động");
        }

        // Check rate limiting - prevent spam
        LocalDateTime oneMinuteAgo = TimeUtil.nowUtc().minusMinutes(1);
        long recentMessageCount = chatMessageRepository.countByUserSince(currentUser, oneMinuteAgo);
        if (recentMessageCount > 10) { // Max 10 messages per minute
            throw new ResourceForbiddenException("Bạn đang gửi tin nhắn quá nhanh, vui lòng chậm lại");
        }

        ChatMessage parentMessage = null;
        if (request.getParentMessageId() != null) {
            parentMessage = chatMessageRepository.findById(request.getParentMessageId())
                    .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", request.getParentMessageId()));
        }

        ChatMessage message = ChatMessage.builder()
                .user(currentUser)
                .chatRoom(chatRoom)
                .parentMessage(parentMessage)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .metadata(request.getMetadata())
                .build();

        message = chatMessageRepository.save(message);
        log.info("Message sent: {} by user: {} in room: {}", message.getId(), currentUser.getId(), chatRoom.getId());

        return mapToResponse(message);
    }

    @Override
    public Page<ChatMessageResponse> getMessagesInRoom(Long chatRoomId, Pageable pageable) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", chatRoomId));

        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(
                chatRoomId, pageable);
        return messages.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ChatMessageResponse editMessage(Long messageId, EditMessageRequest request, User currentUser) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        if (!message.canEdit(currentUser)) {
            throw new ResourceForbiddenException("Không có quyền sửa tin nhắn này");
        }

        message.edit(request.getContent());
        message = chatMessageRepository.save(message);
        log.info("Message edited: {} by user: {}", messageId, currentUser.getId());

        return mapToResponse(message);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, User currentUser) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        if (!message.canDelete(currentUser)) {
            throw new ResourceForbiddenException("Không có quyền xóa tin nhắn này");
        }

        message.delete();
        chatMessageRepository.save(message);
        log.info("Message deleted: {} by user: {}", messageId, currentUser.getId());
    }

    @Override
    @Transactional
    public ChatMessageResponse reactToMessage(Long messageId, ReactToMessageRequest request, User currentUser) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        if (message.getIsDeleted()) {
            throw new ResourceForbiddenException("Không thể phản ứng với tin nhắn đã bị xóa");
        }

        message.addReaction(request.getReactionType());
        message = chatMessageRepository.save(message);
        log.info("Reaction added to message: {} by user: {} type: {}", 
                messageId, currentUser.getId(), request.getReactionType());

        return mapToResponse(message);
    }

    @Override
    @Transactional
    public void removeReaction(Long messageId, String reactionType, User currentUser) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        message.removeReaction(reactionType);
        chatMessageRepository.save(message);
        log.info("Reaction removed from message: {} by user: {} type: {}", 
                messageId, currentUser.getId(), reactionType);
    }

    @Override
    public Page<ChatMessageResponse> getDirectMessages(Long userId, User currentUser, Pageable pageable) {
        // Implementation for direct messages would require a private chat room logic
        // For now, this is a placeholder
        throw new UnsupportedOperationException("Direct messages not implemented yet");
    }

    @Override
    public void sendTypingIndicator(TypingIndicatorRequest request, User currentUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", request.getChatRoomId()));

        // This would typically be handled by WebSocket service
        // For now, this is just a log
        log.info("User {} is {} typing in room {}", 
                currentUser.getId(), 
                request.getIsTyping() ? "started" : "stopped",
                request.getChatRoomId());
    }

    @Override
    public Page<ChatMessageResponse> searchMessages(Long chatRoomId, String keyword, Pageable pageable) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", chatRoomId));

        Page<ChatMessage> messages = chatMessageRepository.searchInChatRoom(chatRoom, keyword, pageable);
        return messages.map(this::mapToResponse);
    }

    @Override
    public ChatMessageResponse getMessageById(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));
        return mapToResponse(message);
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        UserSummaryResponse userResponse = UserSummaryResponse.builder()
                .id(message.getUser().getId())
                .username(message.getUser().getUsername())
                .fullName(message.getUser().getFullName())
                .avatarUrl(message.getUser().getAvatarUrl())
                .isVerified(message.getUser().getIsVerified())
                .build();

        return ChatMessageResponse.builder()
                .id(message.getId())
                .user(userResponse)
                .chatRoomId(message.getChatRoom().getId())
                .parentMessageId(message.getParentMessage() != null ? message.getParentMessage().getId() : null)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .metadata(message.getMetadata())
                .reactions(message.getReactions())
                .isDeleted(message.getIsDeleted())
                .isEdited(message.isEdited())
                .editedAt(message.getEditedAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
