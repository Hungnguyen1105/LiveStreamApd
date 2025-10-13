package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.FollowResponse;
import com.example.livestream_apd.application.dto.response.UserResponse;
import com.example.livestream_apd.application.dto.request.SearchUserRequest;
import com.example.livestream_apd.application.dto.request.UpdateProfileRequest;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.UserRepository;
import com.example.livestream_apd.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    public ApiResponse <UserResponse> getUserProfile(Long userId) {
        try{
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            UserResponse userResponse = mapToUserResponse(user);
            return ApiResponse.success(userResponse);
        }catch (Exception e){
            log.error("lỗi khi lấy ra profile user có id {}",userId, e);
            return ApiResponse.error("không thể thông tin profile"+ e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<UserResponse> updateProfile(Long userId, UpdateProfileRequest request){
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            if (request.getFullName() != null){
                user.setFullName(request.getFullName());
            }

            if (request.getBio() != null){
                user.setBio(request.getBio());
            }

            if (request.getAvatarUrl() != null){
                user.setAvatarUrl(request.getAvatarUrl());
            }

            if (request.getSocialLinks() != null){
                user.setSocialLinks(request.getSocialLinks());
            }

            if (request.getIsPrivate() != null){
                user.setIsPrivate(request.getIsPrivate());
            }

            if (request.getAllowDirectMessages() != null){
                user.setAllowDirectMessages(request.getAllowDirectMessages());
            }

            if (request.getShowOnlineStatus() != null){
                user.setShowOnlineStatus(request.getShowOnlineStatus());
            }

            if (request.getEmailNotifications() != null){
                user.setEmailNotifications(request.getEmailNotifications());
            }

            if (request.getLiveNotifications() != null){
                user.setLiveNotifications(request.getLiveNotifications());
            }

            if (request.getFollowNotifications() != null){
                user.setFollowNotifications(request.getFollowNotifications());
            }

            if (request.getPushNotifications() != null){
                user.setPushNotifications(request.getPushNotifications());
            }

            if (request.getMessageNotifications() != null){
                user.setMessageNotifications(request.getMessageNotifications());
            }

            user.setUpdatedAt(TimeUtil.nowUtc());
            user = userRepository.save(user);
            UserResponse userResponse = mapToUserResponse(user);
            return ApiResponse.success("Cập nhật profile thành công" ,userResponse);

        } catch (Exception e){
            log.error("lỗi khi update profile user có id {}",userId, e);
            return ApiResponse.error("không thể update profile"+ e.getMessage());
        }
    }

    public ApiResponse<UserResponse> getUserById(Long userId, Long currentUserId){
        try{
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            if (!userId.equals(currentUserId) && user.getIsPrivate()){
                boolean isFollowing = userRepository.isFollowing(currentUserId,userId);
                if (!isFollowing){
                    UserResponse userResponse = mapPublicUserResponse(user);
                    return ApiResponse.success(userResponse);
                }
            }
            UserResponse userResponse = mapToUserResponse(user);
            return ApiResponse.success(userResponse);
        } catch (Exception e){
            log.error("Không thể lấy thông tin user {}", userId, e);
            return ApiResponse.error("Không thể lấy thông tin user" + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<String> followUser(Long currentUserId, Long targetUserId){
        if (currentUserId == null || targetUserId == null) {
            return ApiResponse.error("User ID không hợp lệ");
        }
        
        if (currentUserId.equals(targetUserId)){
            return ApiResponse.error("Không thể follow chính mình");
        }
        
        try{
            // Check if already following first
            if (userRepository.isFollowing(currentUserId, targetUserId)){
                return ApiResponse.error("Đã follow user này rồi");
            }
            
            // Verify both users exist
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            User targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("Target user not found"));
            
            // Add follow relationship using repository method
            userRepository.addFollowing(currentUserId, targetUserId);
            
            // Update counters
            currentUser.setFollowingCount(currentUser.getFollowingCount() + 1);
            targetUser.setFollowersCount(targetUser.getFollowersCount() + 1);
            
            userRepository.save(currentUser);
            userRepository.save(targetUser);
            
            return ApiResponse.success("Đã follow thành công");
        }catch (Exception e){
            log.error("Không thể follow user: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể follow user: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<String> unfollowUser(Long currentUserId, Long targetUserId){
        if (currentUserId == null || targetUserId == null) {
            return ApiResponse.error("User ID không hợp lệ");
        }
        
        if (currentUserId.equals(targetUserId)){
            return ApiResponse.error("Không thể unfollow chính mình");
        }
        
        try{
            // Check if currently following
            if (!userRepository.isFollowing(currentUserId, targetUserId)){
                return ApiResponse.error("Chưa follow user này");
            }
            
            // Verify both users exist
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            User targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("Target user not found"));
            
            // Remove follow relationship using repository method
            userRepository.removeFollowing(currentUserId, targetUserId);
            
            // Update counters
            currentUser.setFollowingCount(Math.max(0, currentUser.getFollowingCount() - 1));
            targetUser.setFollowersCount(Math.max(0, targetUser.getFollowersCount() - 1));
            
            userRepository.save(currentUser);
            userRepository.save(targetUser);
            
            return ApiResponse.success("Đã unfollow thành công");
        }catch (Exception e){
            log.error("Không thể unfollow user: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể unfollow user: " + e.getMessage());
        }
    }

    public ApiResponse<Page<FollowResponse>> getFollowers(Long userId, Long currentUserId, int page, int size){
        try{
            // Kiểm tra user exists
            if (!userRepository.existsById(userId)) {
                throw new RuntimeException("User not found");
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<User> followers = userRepository.findFollowersByUserId(userId, pageable);
            Page<FollowResponse> followResponses = followers.map(follower -> mapToFollowResponse(follower,currentUserId));
            return ApiResponse.success(followResponses);
        }catch (Exception e){
            log.error("Không thể lấy danh sách follower");
            return ApiResponse.error("Không thể danh sách follower" +  e.getMessage());
        }
    }

    public ApiResponse<Page<FollowResponse>> getFollowing(Long userId, Long currentUserId, int page, int size){
        try{
            // Kiểm tra user exists
            if (!userRepository.existsById(userId)) {
                throw new RuntimeException("User not found");
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<User> following = userRepository.findFollowingByUserId(userId, pageable);
            Page<FollowResponse> followResponses = following.map(followedUser -> mapToFollowResponse(followedUser,currentUserId));
            return ApiResponse.success(followResponses);
        }catch (Exception e){
            log.error("Không thể lấy danh sách follower");
            return ApiResponse.error("Không thể danh sách follower" +  e.getMessage());
        }
    }

    public ApiResponse<Page<UserResponse>> searchUser(SearchUserRequest request, Long currentUserId){
        try{
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<User> users = userRepository.searchUsers(request.getQuery(),pageable);
            Page<UserResponse> userResponses = users.map(user ->{
                if (user.getIsPrivate() && !user.getId().equals(currentUserId)){
                    boolean isFollowing = userRepository.isFollowing(currentUserId,user.getId());
                    if (!isFollowing){
                        return mapPublicUserResponse(user);
                    }
                }
                return mapToUserResponse(user);
            });
            return ApiResponse.success(userResponses);
        } catch (Exception e){
            log.error("Không thể tìm kiếm User");
            return ApiResponse.error("Không thể tìm kiếm User" +  e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<String> blockUser(Long currentUserId, Long targetUserId){
        try{
            if(currentUserId.equals(targetUserId)){
                return ApiResponse.error("Không thể block chính mình");
            }
            User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("CurrentUser not found"));
            User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("TargetUser not found"));
            if (userRepository.isBlocked(currentUserId,targetUserId)){
                return ApiResponse.error("Đã block User này rồi");
            }
            currentUser.getBlockedUsers().add(targetUser);
            currentUser.getFollowing().remove(targetUser);
            currentUser.getFollowers().remove(targetUser);

            targetUser.getFollowing().remove(currentUser);
            targetUser.getFollowers().remove(currentUser);

            currentUser.setFollowersCount(currentUser.getFollowers().size());
            currentUser.setFollowingCount(currentUser.getFollowing().size());

            targetUser.setFollowersCount(targetUser.getFollowers().size());
            targetUser.setFollowingCount(targetUser.getFollowing().size());

            userRepository.save(currentUser);
            userRepository.save(targetUser);
            return ApiResponse.success("Blocked thành công");

        } catch (Exception e){
            log.error("Không thể chăn User");
            return ApiResponse.error("Không thể chặn User" +  e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<String> unBlockUser(Long currentUserId, Long targetUserId){
        try{
            if(currentUserId.equals(targetUserId)){
                return ApiResponse.error("Không thể unblock ");
            }
            User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("CurrentUser not found"));
            User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("TargetUser not found"));
            if (!userRepository.isBlocked(currentUserId,targetUserId)){
                return ApiResponse.error("Chưa block User này");
            }
            currentUser.getBlockedUsers().remove(targetUser);

            userRepository.save(currentUser);
            return ApiResponse.success("UnBlocked thành công");

        } catch (Exception e){
            log.error("Không thể chăn User");
            return ApiResponse.error("Không thể chặn User" +  e.getMessage());
        }
    }

    private UserResponse mapToUserResponse (User user){
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .bio(user.getBio())
                    .balance(user.getBalance())
                    .isVerified(user.getIsVerified())
                    .isEmailVerified(user.getIsEmailVerified())
                    .isOnline(user.getIsOnline())
                    .lastSeen(user.getLastSeen())
                    .socialLinks(user.getSocialLinks())
                    .status(user.getStatus().name())
                    .followersCount(user.getFollowersCount())
                    .followingCount(user.getFollowingCount())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
    }

    private  UserResponse mapPublicUserResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .isVerified(user.getIsVerified())
                .isOnline(user.getIsOnline())
                .followingCount(user.getFollowingCount())
                .followersCount(user.getFollowersCount())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private FollowResponse mapToFollowResponse(User user, Long currentUserId) {
        return FollowResponse
                .builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .isVerified(user.getIsVerified())
                .isFollowedByCurrentUser(userRepository.isFollowing(currentUserId,user.getId()))
                .isFollowingCurrentUser(userRepository.isFollowing(user.getId(),currentUserId))
                .isOnline(user.getIsOnline())
                .followingCount(user.getFollowingCount())
                .followersCount(user.getFollowersCount())
                .build();
    }
}
