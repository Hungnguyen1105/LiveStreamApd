package com.example.livestream_apd.presentation.controller;

import com.example.livestream_apd.application.dto.request.EditDirectMessageRequest;
import com.example.livestream_apd.application.dto.request.SearchDirectMessageRequest;
import com.example.livestream_apd.application.dto.request.SendDirectMessageRequest;
import com.example.livestream_apd.application.dto.request.StartConversationRequest;
import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.DirectConversationResponse;
import com.example.livestream_apd.application.dto.response.DirectMessageResponse;
import com.example.livestream_apd.application.dto.response.PagedResponse;
import com.example.livestream_apd.domain.entity.DirectMessage;
import com.example.livestream_apd.domain.service.DirectConversationService;
import com.example.livestream_apd.domain.service.DirectMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/direct-messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Direct Messages", description = "API quản lý tin nhắn 1-1")
public class DirectMessageController {

    private final DirectConversationService conversationService;
    private final DirectMessageService messageService;

    // ==================== CONVERSATION ENDPOINTS ====================

    @PostMapping("/conversations")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Tạo hoặc lấy cuộc trò chuyện với user")
    public ResponseEntity<ApiResponse<DirectConversationResponse>> startConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StartConversationRequest request) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<DirectConversationResponse> response = conversationService.startConversation(currentUserId, request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy danh sách cuộc trò chuyện")
    public ResponseEntity<ApiResponse<PagedResponse<DirectConversationResponse>>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Số trang (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "lastMessageAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ApiResponse<PagedResponse<DirectConversationResponse>> response = 
                conversationService.getConversations(currentUserId, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin cuộc trò chuyện")
    public ResponseEntity<ApiResponse<DirectConversationResponse>> getConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<DirectConversationResponse> response = 
                conversationService.getConversation(conversationId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/with/{userId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy cuộc trò chuyện với user cụ thể")
    public ResponseEntity<ApiResponse<DirectConversationResponse>> getConversationWithUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<DirectConversationResponse> response = 
                conversationService.getConversationWithUser(currentUserId, userId);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/conversations/{conversationId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Xóa cuộc trò chuyện")
    public ResponseEntity<ApiResponse<String>> deleteConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<String> response = conversationService.deleteConversation(conversationId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/{conversationId}/block")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Chặn cuộc trò chuyện")
    public ResponseEntity<ApiResponse<String>> blockConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<String> response = conversationService.blockConversation(conversationId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/{conversationId}/unblock")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Bỏ chặn cuộc trò chuyện")
    public ResponseEntity<ApiResponse<String>> unblockConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<String> response = conversationService.unblockConversation(conversationId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Tìm kiếm cuộc trò chuyện")
    public ResponseEntity<ApiResponse<PagedResponse<DirectConversationResponse>>> searchConversations(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String query,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        
        ApiResponse<PagedResponse<DirectConversationResponse>> response = 
                conversationService.searchConversations(currentUserId, query, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/unread")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy cuộc trò chuyện có tin nhắn chưa đọc")
    public ResponseEntity<ApiResponse<PagedResponse<DirectConversationResponse>>> getUnreadConversations(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        
        ApiResponse<PagedResponse<DirectConversationResponse>> response = 
                conversationService.getConversationsWithUnreadMessages(currentUserId, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/unread/count")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Đếm số cuộc trò chuyện chưa đọc")
    public ResponseEntity<ApiResponse<Long>> getUnreadConversationsCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<Long> response = conversationService.getUnreadConversationsCount(currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/{conversationId}/mark-read")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Đánh dấu cuộc trò chuyện đã đọc")
    public ResponseEntity<ApiResponse<String>> markConversationAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<String> response = conversationService.markConversationAsRead(conversationId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    // ==================== MESSAGE ENDPOINTS ====================

    @PostMapping("/messages")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Gửi tin nhắn")
    public ResponseEntity<ApiResponse<DirectMessageResponse>> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendDirectMessageRequest request) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<DirectMessageResponse> response = messageService.sendMessage(currentUserId, request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy danh sách tin nhắn trong cuộc trò chuyện")
    public ResponseEntity<ApiResponse<PagedResponse<DirectMessageResponse>>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ApiResponse<PagedResponse<DirectMessageResponse>> response = 
                messageService.getMessages(conversationId, currentUserId, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages/{messageId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy thông tin tin nhắn")
    public ResponseEntity<ApiResponse<DirectMessageResponse>> getMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<DirectMessageResponse> response = messageService.getMessage(messageId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/messages")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Sửa tin nhắn")
    public ResponseEntity<ApiResponse<DirectMessageResponse>> editMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EditDirectMessageRequest request) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<DirectMessageResponse> response = messageService.editMessage(currentUserId, request);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Xóa tin nhắn")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<String> response = messageService.deleteMessage(messageId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/messages/{messageId}/mark-read")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Đánh dấu tin nhắn đã đọc")
    public ResponseEntity<ApiResponse<String>> markMessageAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<String> response = messageService.markMessageAsRead(messageId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/{conversationId}/messages/mark-all-read")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Đánh dấu tất cả tin nhắn đã đọc")
    public ResponseEntity<ApiResponse<String>> markAllMessagesAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<String> response = messageService.markAllMessagesAsRead(conversationId, currentUserId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/messages/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Tìm kiếm tin nhắn trong cuộc trò chuyện")
    public ResponseEntity<ApiResponse<PagedResponse<DirectMessageResponse>>> searchMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String query,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        SearchDirectMessageRequest request = SearchDirectMessageRequest.builder()
                .query(query)
                .page(page)
                .size(size)
                .build();
        
        ApiResponse<PagedResponse<DirectMessageResponse>> response = 
                messageService.searchMessages(conversationId, currentUserId, request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/messages/media")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy tin nhắn media trong cuộc trò chuyện")
    public ResponseEntity<ApiResponse<PagedResponse<DirectMessageResponse>>> getMediaMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @Parameter(description = "Loại media") @RequestParam(defaultValue = "IMAGE,VIDEO,AUDIO,FILE") String types,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        
        List<DirectMessage.MessageType> mediaTypes = Arrays.stream(types.split(","))
                .map(type -> DirectMessage.MessageType.valueOf(type.trim().toUpperCase()))
                .toList();
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ApiResponse<PagedResponse<DirectMessageResponse>> response = 
                messageService.getMediaMessages(conversationId, currentUserId, mediaTypes, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/messages/new")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Lấy tin nhắn mới sau thời gian nhất định")
    public ResponseEntity<ApiResponse<List<DirectMessageResponse>>> getNewMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @Parameter(description = "Thời gian (ISO format)") @RequestParam String after) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        LocalDateTime afterTime = LocalDateTime.parse(after);
        
        ApiResponse<List<DirectMessageResponse>> response = 
                messageService.getNewMessages(conversationId, currentUserId, afterTime);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/messages/unread/count")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Đếm tin nhắn chưa đọc trong cuộc trò chuyện")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        ApiResponse<Long> response = messageService.getUnreadCount(conversationId, currentUserId);
        
        return ResponseEntity.ok(response);
    }
}
