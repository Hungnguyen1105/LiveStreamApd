package com.example.livestream_apd.presentation.controller;

import com.example.livestream_apd.application.dto.response.PagedResponse;
import com.example.livestream_apd.application.dto.response.PostResponse;
import com.example.livestream_apd.application.dto.response.PostDetailResponse;
import com.example.livestream_apd.application.dto.request.CreatePostRequest;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.UserRepository;
import com.example.livestream_apd.domain.service.PostService;
import com.example.livestream_apd.infrastructure.security.UserDetailsServiceImpl;
import com.example.livestream_apd.utils.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j

public class PostController {
    private final PostService postService;
    private final UserRepository userRepository;

    @GetMapping()
    @Operation(summary = "Thông tin post" , description = "")
    public ResponseEntity<PagedResponse<PostResponse>> getTimelinePosts(
            @CurrentUser User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        if (currentUser == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PagedResponse.<PostResponse>builder()
                            .content(null)
                            .page(0)
                            .size(0)
                            .totalElements(0)
                            .totalPages(0)
                            .first(true)
                            .last(true)
                            .build());
        }
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getTimelinePosts(currentUser, pageable);
        PagedResponse<PostResponse> response = PagedResponse.<PostResponse>builder()
                .content(posts.getContent())
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    @Operation(summary = "Tạo post", description = "")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid@RequestBody CreatePostRequest request ){
        log.info("createPost called - userPrincipal: {}", userPrincipal != null ? userPrincipal.getId() : "NULL");
        if(userPrincipal == null){
            log.error("UserPrincipal is null - authentication failed");
            return ResponseEntity.status((HttpStatus.UNAUTHORIZED)).build();
        }
        
        // Get the User entity from the database
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        PostResponse posts = postService.createPost(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Lấy chi tiết post", description = "")
    public ResponseEntity<PostDetailResponse> getPostById(
            @CurrentUser User currentUser,
            @PathVariable Long postId) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PostDetailResponse postDetail = postService.getPostById(currentUser, postId);
        return ResponseEntity.ok(postDetail);
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "Like post", description = "")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> likePost(
            @CurrentUser User currentUser,
            @PathVariable Long postId) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PostResponse post = postService.likePost(currentUser, postId);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "Unlike post", description = "")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> unlikePost(
            @CurrentUser User currentUser,
            @PathVariable Long postId) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PostResponse post = postService.unlikePost(currentUser, postId);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/{postId}/share")
    @Operation(summary = "Share post", description = "")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostResponse> sharePost(
            @CurrentUser User currentUser,
            @PathVariable Long postId,
            @RequestParam(defaultValue = "INTERNAL") String platform) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PostResponse post = postService.sharePost(currentUser, postId, platform);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/trending")
    @Operation(summary = "Lấy trending posts", description = "")
    public ResponseEntity<PagedResponse<PostResponse>> getTrendingPosts(
            @CurrentUser User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("likeCount").descending());
        Page<PostResponse> posts = postService.getTrendingPosts(currentUser, pageable);
        PagedResponse<PostResponse> response = PagedResponse.<PostResponse>builder()
                .content(posts.getContent())
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Lấy posts của user", description = "")
    public ResponseEntity<PagedResponse<PostResponse>> getUserPosts(
            @CurrentUser User currentUser,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getUserPosts(currentUser, userId, pageable);
        PagedResponse<PostResponse> response = PagedResponse.<PostResponse>builder()
                .content(posts.getContent())
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
        return ResponseEntity.ok(response);
    }

}
