package com.example.livestream_apd.presentation.controller;

import com.example.livestream_apd.application.dto.request.CreateChatRoomRequest;
import com.example.livestream_apd.application.dto.request.EditMessageRequest;
import com.example.livestream_apd.application.dto.request.ReactToMessageRequest;
import com.example.livestream_apd.application.dto.request.SendMessageRequest;
import com.example.livestream_apd.application.dto.request.TypingIndicatorRequest;
import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.ChatMessageResponse;
import com.example.livestream_apd.application.dto.response.ChatRoomResponse;
import com.example.livestream_apd.application.dto.response.PagedResponse;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.UserRepository;
import com.example.livestream_apd.domain.service.ChatMessageService;
import com.example.livestream_apd.domain.service.ChatRoomService;
import com.example.livestream_apd.infrastructure.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat API", description = "Real-time Chat & Messaging APIs")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    @GetMapping("/rooms")
    @Operation(summary = "Lấy danh sách chat rooms", description = "Trả về danh sách tất cả phòng chat hoạt động")
    public ResponseEntity<ApiResponse<PagedResponse<ChatRoomResponse>>> getChatRooms(
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số phần tử mỗi trang")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ChatRoomResponse> chatRooms = chatRoomService.getChatRooms(pageable);
        PagedResponse<ChatRoomResponse> response = PagedResponse.<ChatRoomResponse>builder()
                .content(chatRooms.getContent())
                .size(chatRooms.getSize())
                .page(chatRooms.getNumber())
                .totalElements(chatRooms.getTotalElements())
                .totalPages(chatRooms.getTotalPages())
                .last(chatRooms.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/rooms")
    @Operation(summary = "Tạo chat room mới", description = "Tạo phòng chat mới (chỉ moderator/admin)")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid @RequestBody CreateChatRoomRequest request) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoomResponse response = chatRoomService.createChatRoom(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/rooms/{id}/messages")
    @Operation(summary = "Lấy tin nhắn trong room", description = "Lấy danh sách tin nhắn trong phòng chat")
    public ResponseEntity<ApiResponse<PagedResponse<ChatMessageResponse>>> getMessagesInRoom(
            @PathVariable Long id,
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số phần tử mỗi trang")
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ChatMessageResponse> messages = chatMessageService.getMessagesInRoom(id, pageable);
        PagedResponse<ChatMessageResponse> response = PagedResponse.<ChatMessageResponse>builder()
                .content(messages.getContent())
                .size(messages.getSize())
                .page(messages.getNumber())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .last(messages.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/rooms/{id}/messages")
    @Operation(summary = "Gửi tin nhắn", description = "Gửi tin nhắn mới vào phòng chat")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid @RequestBody SendMessageRequest request) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set chat room ID from path variable
        request.setChatRoomId(id);

        ChatMessageResponse response = chatMessageService.sendMessage(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/messages/{id}")
    @Operation(summary = "Xóa tin nhắn", description = "Xóa tin nhắn (chỉ người gửi hoặc moderator)")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatMessageService.deleteMessage(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Tin nhắn đã được xóa"));
    }

    @PutMapping("/messages/{id}")
    @Operation(summary = "Sửa tin nhắn", description = "Sửa nội dung tin nhắn (chỉ người gửi)")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> editMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid @RequestBody EditMessageRequest request) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessageResponse response = chatMessageService.editMessage(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/messages/{id}/react")
    @Operation(summary = "React tin nhắn", description = "Thêm phản ứng vào tin nhắn")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> reactToMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid @RequestBody ReactToMessageRequest request) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessageResponse response = chatMessageService.reactToMessage(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/messages/{id}/react/{reactionType}")
    @Operation(summary = "Bỏ react tin nhắn", description = "Xóa phản ứng khỏi tin nhắn")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> removeReaction(
            @PathVariable Long id,
            @PathVariable String reactionType,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatMessageService.removeReaction(id, reactionType, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Phản ứng đã được xóa"));
    }

    @GetMapping("/direct/{userId}")
    @Operation(summary = "Chat trực tiếp với user", description = "Chuyển hướng đến API Direct Messages")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> getDirectMessages(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal) {

        // This endpoint redirects to the new Direct Message API
        return ResponseEntity.ok(
            ApiResponse.success("Vui lòng sử dụng API mới: /api/v1/direct-messages/conversations/with/" + userId)
        );
    }

    @PostMapping("/typing")
    @Operation(summary = "Gửi typing indicator", description = "Gửi thông báo đang nhập tin nhắn")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> sendTypingIndicator(
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid @RequestBody TypingIndicatorRequest request) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatMessageService.sendTypingIndicator(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Typing indicator sent"));
    }

    @GetMapping("/rooms/{id}/search")
    @Operation(summary = "Tìm kiếm tin nhắn", description = "Tìm kiếm tin nhắn trong phòng chat")
    public ResponseEntity<ApiResponse<PagedResponse<ChatMessageResponse>>> searchMessages(
            @PathVariable Long id,
            @RequestParam String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số phần tử mỗi trang")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ChatMessageResponse> messages = chatMessageService.searchMessages(id, keyword, pageable);
        PagedResponse<ChatMessageResponse> response = PagedResponse.<ChatMessageResponse>builder()
                .content(messages.getContent())
                .size(messages.getSize())
                .page(messages.getNumber())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .last(messages.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
