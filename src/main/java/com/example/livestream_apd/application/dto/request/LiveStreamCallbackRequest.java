package com.example.livestream_apd.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class LiveStreamCallbackRequest {
    
    @NotBlank(message = "Room ID is required")
    private String roomId;
    
    @NotBlank(message = "Action is required") 
    private String action; // "start", "end", "join", "leave", "gift", "analytics"
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private String targetUserId; // For gifts or interactions
    
    private Long duration; // Duration in seconds for "end" action
    
    private Integer viewerCount;
    
    private Integer maxViewers;
    
    private Integer totalViews;
    
    private String giftId;
    
    private Integer giftValue;
    
    private Integer giftQuantity;
    
    private String title;
    
    private Map<String, Object> additionalData;
    
    private Map<String, Object> statistics;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}