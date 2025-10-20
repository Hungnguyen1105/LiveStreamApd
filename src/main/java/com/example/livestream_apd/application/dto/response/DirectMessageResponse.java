package com.example.livestream_apd.application.dto.response;

import com.example.livestream_apd.domain.entity.DirectMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageResponse {
    
    private Long id;
    private Long conversationId;
    private UserResponse sender;
    private String content;
    private DirectMessage.MessageType messageType;
    private String mediaUrl;
    private String thumbnailUrl;
    private Long mediaSize;
    private String mediaType;
    private DirectMessageResponse replyToMessage;
    private Boolean isEdited;
    private LocalDateTime editedAt;
    private Boolean isDeleted;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
