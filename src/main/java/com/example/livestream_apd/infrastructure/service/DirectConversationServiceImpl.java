package com.example.livestream_apd.infrastructure.service;

import com.example.livestream_apd.application.dto.request.StartConversationRequest;
import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.DirectConversationResponse;
import com.example.livestream_apd.application.dto.response.DirectMessageResponse;
import com.example.livestream_apd.application.dto.response.PagedResponse;
import com.example.livestream_apd.application.dto.response.UserResponse;
import com.example.livestream_apd.domain.entity.DirectConversation;
import com.example.livestream_apd.domain.entity.DirectMessage;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.DirectConversationRepository;
import com.example.livestream_apd.domain.repository.DirectMessageRepository;
import com.example.livestream_apd.domain.repository.UserRepository;
import com.example.livestream_apd.domain.service.DirectConversationService;
import com.example.livestream_apd.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectConversationServiceImpl implements DirectConversationService {

    private final DirectConversationRepository conversationRepository;
    private final DirectMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<DirectConversationResponse> startConversation(Long currentUserId, StartConversationRequest request) {
        try {
            if (currentUserId.equals(request.getTargetUserId())) {
                return ApiResponse.error("Không thể tạo cuộc trò chuyện với chính mình");
            }

            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));
            
            User targetUser = userRepository.findById(request.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException("Target user không tồn tại"));

            // Check if conversation already exists
            Optional<DirectConversation> existingConversation = 
                    conversationRepository.findByTwoUsers(currentUserId, request.getTargetUserId());

            DirectConversation conversation;
            if (existingConversation.isPresent()) {
                conversation = existingConversation.get();
                // Restore conversation if deleted by current user
                if (conversation.isDeletedBy(currentUser)) {
                    conversation.restoreBy(currentUser);
                    conversationRepository.save(conversation);
                }
            } else {
                conversation = DirectConversation.builder()
                        .user1(currentUser)
                        .user2(targetUser)
                        .build();
                conversation = conversationRepository.save(conversation);
            }

            // Send initial message if provided
            if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
                DirectMessage message = DirectMessage.builder()
                        .conversation(conversation)
                        .sender(currentUser)
                        .content(request.getInitialMessage().trim())
                        .messageType(DirectMessage.MessageType.TEXT)
                        .build();
                
                messageRepository.save(message);
                conversation.addMessage(message);
                conversationRepository.save(conversation);
            }

            DirectConversationResponse response = mapToConversationResponse(conversation, currentUser);
            return ApiResponse.success("Cuộc trò chuyện đã được tạo thành công", response);

        } catch (Exception e) {
            log.error("Lỗi khi tạo cuộc trò chuyện: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể tạo cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<DirectConversationResponse>> getConversations(Long userId, Pageable pageable) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            Page<DirectConversation> conversations = conversationRepository.findActiveConversationsByUserId(userId, pageable);
            
            Page<DirectConversationResponse> conversationResponses = conversations.map(conversation -> 
                    mapToConversationResponse(conversation, user));

            PagedResponse<DirectConversationResponse> pagedResponse = PagedResponse.<DirectConversationResponse>builder()
                    .content(conversationResponses.getContent())
                    .page(conversationResponses.getNumber())
                    .size(conversationResponses.getSize())
                    .totalElements(conversationResponses.getTotalElements())
                    .totalPages(conversationResponses.getTotalPages())
                    .first(conversationResponses.isFirst())
                    .last(conversationResponses.isLast())
                    .build();

            return ApiResponse.success(pagedResponse);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách cuộc trò chuyện: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy danh sách cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<DirectConversationResponse> getConversation(Long conversationId, Long currentUserId) {
        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            DirectConversation conversation = conversationRepository.findActiveConversationByIdAndUserId(conversationId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            DirectConversationResponse response = mapToConversationResponse(conversation, currentUser);
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy cuộc trò chuyện: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<DirectConversationResponse> getConversationWithUser(Long currentUserId, Long targetUserId) {
        try {
            if (currentUserId.equals(targetUserId)) {
                return ApiResponse.error("Không thể tạo cuộc trò chuyện với chính mình");
            }

            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            Optional<DirectConversation> conversation = conversationRepository.findByTwoUsers(currentUserId, targetUserId);
            
            if (conversation.isPresent() && !conversation.get().isDeletedBy(currentUser)) {
                DirectConversationResponse response = mapToConversationResponse(conversation.get(), currentUser);
                return ApiResponse.success(response);
            } else {
                return ApiResponse.error("Cuộc trò chuyện không tồn tại");
            }

        } catch (Exception e) {
            log.error("Lỗi khi lấy cuộc trò chuyện với user: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteConversation(Long conversationId, Long currentUserId) {
        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            DirectConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

            if (!conversation.isParticipant(currentUser)) {
                return ApiResponse.error("Bạn không có quyền xóa cuộc trò chuyện này");
            }

            conversation.deleteBy(currentUser);
            conversationRepository.save(conversation);

            return ApiResponse.success("Cuộc trò chuyện đã được xóa thành công");

        } catch (Exception e) {
            log.error("Lỗi khi xóa cuộc trò chuyện: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể xóa cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> blockConversation(Long conversationId, Long currentUserId) {
        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            DirectConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

            if (!conversation.isParticipant(currentUser)) {
                return ApiResponse.error("Bạn không có quyền chặn cuộc trò chuyện này");
            }

            conversation.blockBy(currentUser);
            conversationRepository.save(conversation);

            return ApiResponse.success("Cuộc trò chuyện đã được chặn");

        } catch (Exception e) {
            log.error("Lỗi khi chặn cuộc trò chuyện: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể chặn cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> unblockConversation(Long conversationId, Long currentUserId) {
        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            DirectConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

            if (!conversation.isParticipant(currentUser)) {
                return ApiResponse.error("Bạn không có quyền bỏ chặn cuộc trò chuyện này");
            }

            conversation.unblockBy(currentUser);
            conversationRepository.save(conversation);

            return ApiResponse.success("Cuộc trò chuyện đã được bỏ chặn");

        } catch (Exception e) {
            log.error("Lỗi khi bỏ chặn cuộc trò chuyện: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể bỏ chặn cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<DirectConversationResponse>> searchConversations(Long userId, String query, Pageable pageable) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            Page<DirectConversation> conversations = conversationRepository.searchConversations(userId, query, pageable);
            
            Page<DirectConversationResponse> conversationResponses = conversations.map(conversation -> 
                    mapToConversationResponse(conversation, user));

            PagedResponse<DirectConversationResponse> pagedResponse = PagedResponse.<DirectConversationResponse>builder()
                    .content(conversationResponses.getContent())
                    .page(conversationResponses.getNumber())
                    .size(conversationResponses.getSize())
                    .totalElements(conversationResponses.getTotalElements())
                    .totalPages(conversationResponses.getTotalPages())
                    .first(conversationResponses.isFirst())
                    .last(conversationResponses.isLast())
                    .build();

            return ApiResponse.success(pagedResponse);

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm cuộc trò chuyện: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể tìm kiếm cuộc trò chuyện: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<DirectConversationResponse>> getConversationsWithUnreadMessages(Long userId, Pageable pageable) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            Page<DirectConversation> conversations = conversationRepository.findConversationsWithUnreadMessages(userId, pageable);
            
            Page<DirectConversationResponse> conversationResponses = conversations.map(conversation -> 
                    mapToConversationResponse(conversation, user));

            PagedResponse<DirectConversationResponse> pagedResponse = PagedResponse.<DirectConversationResponse>builder()
                    .content(conversationResponses.getContent())
                    .page(conversationResponses.getNumber())
                    .size(conversationResponses.getSize())
                    .totalElements(conversationResponses.getTotalElements())
                    .totalPages(conversationResponses.getTotalPages())
                    .first(conversationResponses.isFirst())
                    .last(conversationResponses.isLast())
                    .build();

            return ApiResponse.success(pagedResponse);

        } catch (Exception e) {
            log.error("Lỗi khi lấy cuộc trò chuyện có tin nhắn chưa đọc: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy cuộc trò chuyện có tin nhắn chưa đọc: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Long> getUnreadConversationsCount(Long userId) {
        try {
            Long count = conversationRepository.countConversationsWithUnreadMessages(userId);
            return ApiResponse.success(count);

        } catch (Exception e) {
            log.error("Lỗi khi đếm cuộc trò chuyện chưa đọc: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể đếm cuộc trò chuyện chưa đọc: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> markConversationAsRead(Long conversationId, Long currentUserId) {
        try {
            DirectConversation conversation = conversationRepository.findActiveConversationByIdAndUserId(conversationId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại hoặc bạn không có quyền truy cập"));

            int updatedCount = messageRepository.markAllAsRead(conversationId, currentUserId, TimeUtil.nowUtc());
            
            return ApiResponse.success("Đã đánh dấu " + updatedCount + " tin nhắn là đã đọc");

        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu cuộc trò chuyện đã đọc: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể đánh dấu cuộc trò chuyện đã đọc: " + e.getMessage());
        }
    }

    private DirectConversationResponse mapToConversationResponse(DirectConversation conversation, User currentUser) {
        User otherUser = conversation.getOtherUser(currentUser);
        UserResponse otherUserResponse = mapToUserResponse(otherUser);
        
        DirectMessageResponse lastMessageResponse = null;
        Optional<DirectMessage> lastMessage = messageRepository.findLastMessage(conversation.getId());
        if (lastMessage.isPresent()) {
            lastMessageResponse = mapToMessageResponse(lastMessage.get());
        }

        Long unreadCount = conversationRepository.countUnreadMessages(conversation.getId(), currentUser.getId());
        
        return DirectConversationResponse.builder()
                .id(conversation.getId())
                .otherUser(otherUserResponse)
                .lastMessage(lastMessageResponse)
                .unreadCount(unreadCount)
                .isBlocked(conversation.isBlockedBy(currentUser))
                .isBlockedByOther(conversation.isBlockedBy(otherUser))
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
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

    private DirectMessageResponse mapToMessageResponse(DirectMessage message) {
        DirectMessageResponse replyToResponse = null;
        if (message.getReplyToMessage() != null) {
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
}
