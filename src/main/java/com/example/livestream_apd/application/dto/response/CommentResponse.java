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
public class CommentResponse {
    private Long id;
    private UserSummaryResponse user;
    private String content;
    private Integer likeCount;
    private Boolean isPinned;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();
}
