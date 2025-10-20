package com.example.livestream_apd.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private UserSummaryResponse user;
    private String caption;
    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();
    private String locations;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Boolean isPublic;
    private Boolean isLiked;
    private Boolean isShared;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
