package com.example.livestream_apd.presentation.controller;

import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.FollowResponse;
import com.example.livestream_apd.application.dto.response.UserResponse;
import com.example.livestream_apd.application.dto.request.SearchUserRequest;
import com.example.livestream_apd.application.dto.request.UpdateProfileRequest;
import com.example.livestream_apd.domain.service.UserService;
import com.example.livestream_apd.infrastructure.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "")
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    @Operation (summary = "Thông tin profile" , description = "")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity <ApiResponse <UserResponse>> getProfile(@AuthenticationPrincipal
                                                                  UserDetailsServiceImpl.UserPrincipal userPrincipal){
        ApiResponse<UserResponse> response = userService.getUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "cập nhật profile", description = "")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity <ApiResponse <UserResponse>> updateProfile(@Valid @RequestBody
                             UpdateProfileRequest request,
                              @AuthenticationPrincipal
                              UserDetailsServiceImpl.UserPrincipal userPrincipal
                                                                              ){
        ApiResponse<UserResponse> response = userService.updateProfile(userPrincipal.getId(),request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation (summary = "Thông tin user khác" , description = "")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity <ApiResponse<UserResponse>> getUserById(@PathVariable Long id,
    @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal currentUserId){
        ApiResponse<UserResponse> response = userService.getUserById(id,currentUserId.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/follow")
    @Operation (summary = "follow User" , description = "")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<ApiResponse<String>> followUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal currentUserId){
        ApiResponse<String> response = userService.followUser(currentUserId.getId(),id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping ("/{id}/follow")
    @Operation (summary = "follow User" , description = "")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<ApiResponse<String>> unFollowUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal currentUserId){
        ApiResponse<String> response = userService.unfollowUser(currentUserId.getId(),id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/followers")
    @Operation (summary = "Danh sách follower" , description = "")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<ApiResponse<Page<FollowResponse>>> getFollowers(
        @PathVariable Long id,
        @RequestParam (defaultValue = "0") int page, @RequestParam (defaultValue = "10") int size,
        @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal currentUser){

        ApiResponse<Page<FollowResponse>> response = userService.getFollowers(id, currentUser.getId(),  page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/following")
    @Operation (summary = "Danh sách following" , description = "")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<ApiResponse<Page<FollowResponse>>> getFollowing(
            @PathVariable Long id,
            @RequestParam (defaultValue = "0") int page, @RequestParam (defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal currentUser){

        ApiResponse<Page<FollowResponse>> response = userService.getFollowing(id, currentUser.getId(),  page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation (summary = "Tìm kiếm User" , description = "")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String query,
            @RequestParam (defaultValue = "0") int page, @RequestParam (defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal currentUser){
        SearchUserRequest request = new SearchUserRequest();
        request.setQuery(query);
        request.setPage(page);
        request.setSize(size);
        ApiResponse<Page<UserResponse>> response = userService.searchUser(request,currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/block/{id}")
    @Operation (summary = "Block User" , description = "")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<ApiResponse<String>> blockUser(
           @PathVariable Long id,
           @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal currentUser){
        ApiResponse<String> response = userService.blockUser(currentUser.getId(),id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/block/{id}")
    @Operation (summary = "Unblock User" , description = "")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<ApiResponse<String>> unBlockUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal currentUser){
        ApiResponse<String> response = userService.unBlockUser(currentUser.getId(),id);
        return ResponseEntity.ok(response);
    }
}
