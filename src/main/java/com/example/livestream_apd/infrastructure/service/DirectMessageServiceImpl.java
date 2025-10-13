package com.example.livestream_apd.infrastructure.service;

import com.example.livestream_apd.application.dto.request.EditDirectMessageRequest;
import com.example.livestream_apd.application.dto.request.SearchDirectMessageRequest;
import com.example.livestream_apd.application.dto.request.SendDirectMessageRequest;
import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.DirectMessageResponse;
import com.example.livestream_apd.application.dto.response.PagedResponse;
import com.example.livestream_apd.application.dto.response.UserResponse;
import com.example.livestream_apd.domain.entity.DirectConversation;
import com.example.livestream_apd.domain.entity.DirectMessage;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.DirectConversationRepository;
import com.example.livestream_apd.domain.repository.DirectMessageRepository;
import com.example.livestream_apd.domain.repository.UserRepository;
import com.example.livestream_apd.domain.service.DirectMessageService;
import com.example.livestream_apd.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageRepository messageRepository;
    private final DirectConversationRepository conversationRepository;
    private final UserRepository userRepository;

    // Rate limiting constants
    private static final int MAX_MESSAGES_PER_MINUTE = 30;
    private static final int MAX_MESSAGE_LENGTH = 4000;

    @Override
    @Transactional
    public ApiResponse<DirectMessageResponse> sendMessage(Long currentUserId, SendDirectMessageRequest request) {
        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            DirectConversation conversation = conversationRepository.findActiveConversationByIdAndUserId(
                    request.getConversationId(), currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            // Check if conversation is blocked
            if (conversation.isBlockedBy(currentUser)) {
                return ApiResponse.error("Bạn đã chặn cuộc trò chuyện này");
            }

            User otherUser = conversation.getOtherUser(currentUser);
            if (conversation.isBlockedBy(otherUser)) {
                return ApiResponse.error("Cuộc trò chuyện đã bị chặn bởi người dùng khác");
            }

            // Rate limiting check
            LocalDateTime oneMinuteAgo = TimeUtil.nowUtc().minusMinutes(1);
            List<DirectMessage> recentMessages = messageRepository.findRecentMessagesBySender(
                    request.getConversationId(), currentUserId, oneMinuteAgo);
            
            if (recentMessages.size() >= MAX_MESSAGES_PER_MINUTE) {
                return ApiResponse.error("Bạn đang gửi tin nhắn quá nhanh. Vui lòng chờ một chút.");
            }

            // Validate message content
            if (request.getContent().length() > MAX_MESSAGE_LENGTH) {
                return ApiResponse.error("Tin nhắn quá dài. Tối đa " + MAX_MESSAGE_LENGTH + " ký tự.");
            }

            DirectMessage replyToMessage = null;
            if (request.getReplyToMessageId() != null) {
                replyToMessage = messageRepository.findByIdAndConversationId(
                        request.getReplyToMessageId(), request.getConversationId())
                        .orElse(null);
            }

            DirectMessage message = DirectMessage.builder()
                    .conversation(conversation)
                    .sender(currentUser)
                    .content(request.getContent().trim())
                    .messageType(request.getMessageType())
                    .mediaUrl(request.getMediaUrl())
                    .thumbnailUrl(request.getThumbnailUrl())
                    .mediaSize(request.getMediaSize())
                    .mediaType(request.getMediaType())
                    .replyToMessage(replyToMessage)
                    .build();

            message = messageRepository.save(message);
            
            // Update conversation's last message time
            conversation.setLastMessageAt(message.getCreatedAt());
            conversationRepository.save(conversation);

            DirectMessageResponse response = mapToMessageResponse(message);
            return ApiResponse.success("Tin nhắn đã được gửi thành công", response);

        } catch (Exception e) {
            log.error("Lỗi khi gửi tin nhắn: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể gửi tin nhắn: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<DirectMessageResponse>> getMessages(Long conversationId, Long currentUserId, Pageable pageable) {
        try {
            conversationRepository.findActiveConversationByIdAndUserId(conversationId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            Page<DirectMessage> messages = messageRepository.findByConversationIdAndNotDeleted(conversationId, pageable);
            
            Page<DirectMessageResponse> messageResponses = messages.map(this::mapToMessageResponse);

            PagedResponse<DirectMessageResponse> pagedResponse = PagedResponse.<DirectMessageResponse>builder()
                    .content(messageResponses.getContent())
                    .page(messageResponses.getNumber())
                    .size(messageResponses.getSize())
                    .totalElements(messageResponses.getTotalElements())
                    .totalPages(messageResponses.getTotalPages())
                    .first(messageResponses.isFirst())
                    .last(messageResponses.isLast())
                    .build();

            return ApiResponse.success(pagedResponse);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy tin nhắn: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<DirectMessageResponse> getMessage(Long messageId, Long currentUserId) {
        try {
            DirectMessage message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

            // Check if user has access to this conversation
            conversationRepository.findActiveConversationByIdAndUserId(message.getConversation().getId(), currentUserId)
                    .orElseThrow(() -> new RuntimeException("Bạn không có quyền truy cập tin nhắn này"));

            if (message.getIsDeleted()) {
                return ApiResponse.error("Tin nhắn đã bị xóa");
            }

            DirectMessageResponse response = mapToMessageResponse(message);
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy tin nhắn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<DirectMessageResponse> editMessage(Long currentUserId, EditDirectMessageRequest request) {
        try {
            DirectMessage message = messageRepository.findById(request.getMessageId())
                    .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

            if (!message.canBeEditedBy(userRepository.findById(currentUserId).orElseThrow())) {
                return ApiResponse.error("Bạn không có quyền sửa tin nhắn này");
            }

            if (request.getContent().length() > MAX_MESSAGE_LENGTH) {
                return ApiResponse.error("Tin nhắn quá dài. Tối đa " + MAX_MESSAGE_LENGTH + " ký tự.");
            }

            message.setContent(request.getContent().trim());
            message.markAsEdited();
            message = messageRepository.save(message);

            DirectMessageResponse response = mapToMessageResponse(message);
            return ApiResponse.success("Tin nhắn đã được sửa thành công", response);

        } catch (Exception e) {
            log.error("Lỗi khi sửa tin nhắn: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể sửa tin nhắn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteMessage(Long messageId, Long currentUserId) {
        try {
            DirectMessage message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

            User currentUser = userRepository.findById(currentUserId).orElseThrow();
            if (!message.canBeDeletedBy(currentUser)) {
                return ApiResponse.error("Bạn không có quyền xóa tin nhắn này");
            }

            message.markAsDeleted();
            messageRepository.save(message);

            return ApiResponse.success("Tin nhắn đã được xóa thành công");

        } catch (Exception e) {
            log.error("Lỗi khi xóa tin nhắn: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể xóa tin nhắn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> markMessageAsRead(Long messageId, Long currentUserId) {
        try {
            DirectMessage message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

            // Check if user has access to this conversation
            conversationRepository.findActiveConversationByIdAndUserId(message.getConversation().getId(), currentUserId)
                    .orElseThrow(() -> new RuntimeException("Bạn không có quyền truy cập tin nhắn này"));

            // Only mark as read if current user is not the sender
            if (!message.getSender().getId().equals(currentUserId) && !message.getIsRead()) {
                message.markAsRead();
                messageRepository.save(message);
            }

            return ApiResponse.success("Tin nhắn đã được đánh dấu là đã đọc");

        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tin nhắn đã đọc: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể đánh dấu tin nhắn đã đọc: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> markAllMessagesAsRead(Long conversationId, Long currentUserId) {
        try {
            conversationRepository.findActiveConversationByIdAndUserId(conversationId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            int updatedCount = messageRepository.markAllAsRead(conversationId, currentUserId, TimeUtil.nowUtc());
            
            return ApiResponse.success("Đã đánh dấu " + updatedCount + " tin nhắn là đã đọc");

        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tất cả tin nhắn đã đọc: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể đánh dấu tất cả tin nhắn đã đọc: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<DirectMessageResponse>> searchMessages(
            Long conversationId, Long currentUserId, SearchDirectMessageRequest request) {
        try {
            conversationRepository.findActiveConversationByIdAndUserId(conversationId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            Page<DirectMessage> messages = messageRepository.searchInConversation(
                    conversationId, request.getQuery(), 
                    org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize()));
            
            Page<DirectMessageResponse> messageResponses = messages.map(this::mapToMessageResponse);

            PagedResponse<DirectMessageResponse> pagedResponse = PagedResponse.<DirectMessageResponse>builder()
                    .content(messageResponses.getContent())
                    .page(messageResponses.getNumber())
                    .size(messageResponses.getSize())
                    .totalElements(messageResponses.getTotalElements())
                    .totalPages(messageResponses.getTotalPages())
                    .first(messageResponses.isFirst())
                    .last(messageResponses.isLast())
                    .build();

            return ApiResponse.success(pagedResponse);

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm tin nhắn: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể tìm kiếm tin nhắn: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<DirectMessageResponse>> getMediaMessages(
            Long conversationId, Long currentUserId, 
            List<DirectMessage.MessageType> mediaTypes, Pageable pageable) {
        try {
            conversationRepository.findActiveConversationByIdAndUserId(conversationId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            Page<DirectMessage> messages = messageRepository.findMediaMessages(conversationId, mediaTypes, pageable);
            
            Page<DirectMessageResponse> messageResponses = messages.map(this::mapToMessageResponse);

            PagedResponse<DirectMessageResponse> pagedResponse = PagedResponse.<DirectMessageResponse>builder()
                    .content(messageResponses.getContent())
                    .page(messageResponses.getNumber())
                    .size(messageResponses.getSize())
                    .totalElements(messageResponses.getTotalElements())
                    .totalPages(messageResponses.getTotalPages())
                    .first(messageResponses.isFirst())
                    .last(messageResponses.isLast())
                    .build();

            return ApiResponse.success(pagedResponse);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn media: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy tin nhắn media: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<DirectMessageResponse>> getNewMessages(
            Long conversationId, Long currentUserId, LocalDateTime after) {
        try {
            conversationRepository.findActiveConversationByIdAndUserId(conversationId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            List<DirectMessage> messages = messageRepository.findByConversationIdAfterTime(conversationId, after);
            
            List<DirectMessageResponse> messageResponses = messages.stream()
                    .map(this::mapToMessageResponse)
                    .collect(Collectors.toList());

            return ApiResponse.success(messageResponses);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn mới: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy tin nhắn mới: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Long> getUnreadCount(Long conversationId, Long currentUserId) {
        try {
            conversationRepository.findActiveConversationByIdAndUserId(conversationId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            Long count = messageRepository.countUnreadMessages(conversationId, currentUserId);
            return ApiResponse.success(count);

        } catch (Exception e) {
            log.error("Lỗi khi đếm tin nhắn chưa đọc: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể đếm tin nhắn chưa đọc: " + e.getMessage());
        }
    }

    private DirectMessageResponse mapToMessageResponse(DirectMessage message) {
        DirectMessageResponse replyToResponse = null;
        if (message.getReplyToMessage() != null && !message.getReplyToMessage().getIsDeleted()) {
            replyToResponse = mapToMessageResponse(message.getReplyToMessage());
        }

        return DirectMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .sender(mapToUserResponse(message.getSender()))
                .content(message.getContent())
                .messageType(message.getMessageType())
                .mediaUrl(message.getMediaUrl())
                .thumbnailUrl(message.getThumbnailUrl())
                .mediaSize(message.getMediaSize())
                .mediaType(message.getMediaType())
                .replyToMessage(replyToResponse)
                .isEdited(message.getIsEdited())
                .editedAt(message.getEditedAt())
                .isDeleted(message.getIsDeleted())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .isOnline(user.getIsOnline())
                .isVerified(user.getIsVerified())
                .build();
    }
}
