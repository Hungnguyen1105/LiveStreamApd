package com.example.livestream_apd.application.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LiveStreamSessionResponse {
    private String sessionId;
    private String roomId;
    private String hostId;
    private String hostUsername;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // in seconds
    private Integer maxViewers;
    private Integer totalViews;
    private Double totalGiftsValue;
    private String status; // "active", "ended"
}