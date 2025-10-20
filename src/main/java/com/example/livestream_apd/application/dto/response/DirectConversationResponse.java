package com.example.livestream_apd.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectConversationResponse {
    
    private Long id;
    private UserResponse otherUser;
    private DirectMessageResponse lastMessage;
    private Long unreadCount;
    private Boolean isBlocked;
    private Boolean isBlockedByOther;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
