package com.example.livestream_apd.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowResponse {
    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;
    private Boolean isVerified;
    private Boolean isFollowedByCurrentUser;
    private Boolean isFollowingCurrentUser;
    private String bio;
    private Integer followersCount;
    private Integer followingCount;
    private Boolean isOnline;
}
