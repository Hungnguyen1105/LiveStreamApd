package com.example.livestream_apd.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {
    private Long id;
    private UserSummaryResponse user;
    private String caption;
    private List<String> mediaUrls;
    private String location;
    private List<String> tags;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Boolean isPublic;
    private Boolean isLiked;
    private Boolean isShared;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
