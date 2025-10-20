package com.example.livestream_apd.presentation.controller;

import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.domain.entity.LiveStreamSession;
import com.example.livestream_apd.domain.service.LiveStreamService;
import com.example.livestream_apd.application.dto.request.LiveStreamCallbackRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/livestream")
@Tag(name = "Live Stream", description = "Live Stream Management APIs")
public class LiveStreamController {

    @Autowired
    private LiveStreamService liveStreamService;

    @PostMapping("/room/created")
    @Operation(summary = "Handle room created callback")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LiveStreamSession>> handleRoomCreated(
            @Valid @RequestBody LiveStreamCallbackRequest request) {
        
        LiveStreamSession session = liveStreamService.handleRoomCreated(request);
        
        return ResponseEntity.ok(ApiResponse.success("Room created callback processed successfully", session));
    }

    @PostMapping("/room/destroyed")
    @Operation(summary = "Handle room destroyed callback")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LiveStreamSession>> handleRoomDestroyed(
            @Valid @RequestBody LiveStreamCallbackRequest request) {
        
        LiveStreamSession session = liveStreamService.handleRoomDestroyed(request);
        
        return ResponseEntity.ok(ApiResponse.success("Room destroyed callback processed successfully", session));
    }

    @PostMapping("/user/joined")
    @Operation(summary = "Handle user joined callback")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LiveStreamSession>> handleUserJoined(
            @Valid @RequestBody LiveStreamCallbackRequest request) {
        
        LiveStreamSession session = liveStreamService.handleUserJoined(request);
        
        return ResponseEntity.ok(ApiResponse.success("User joined callback processed successfully", session));
    }

    @PostMapping("/user/left")
    @Operation(summary = "Handle user left callback")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LiveStreamSession>> handleUserLeft(
            @Valid @RequestBody LiveStreamCallbackRequest request) {
        
        LiveStreamSession session = liveStreamService.handleUserLeft(request);
        
        return ResponseEntity.ok(ApiResponse.success("User left callback processed successfully", session));
    }

    @PostMapping("/gift/sent")
    @Operation(summary = "Handle gift sent callback")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> handleGiftSent(
            @Valid @RequestBody LiveStreamCallbackRequest request) {
        
        liveStreamService.handleGiftSent(request);
        
        return ResponseEntity.ok(ApiResponse.success("Gift sent callback processed successfully", null));
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Get live stream session by room ID")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LiveStreamSession>> getLiveStreamSession(
            @PathVariable String roomId) {
        
        LiveStreamSession session = liveStreamService.getSessionByRoomId(roomId);
        
        return ResponseEntity.ok(ApiResponse.success("Live stream session retrieved successfully", session));
    }

    @GetMapping("/user/{userId}/sessions")
    @Operation(summary = "Get user's live stream sessions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getUserSessions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Object sessions = liveStreamService.getUserSessions(userId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success("User sessions retrieved successfully", sessions));
    }
}