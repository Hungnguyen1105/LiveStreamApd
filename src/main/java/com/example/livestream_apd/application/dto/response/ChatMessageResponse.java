package com.example.livestream_apd.application.dto.response;

import com.example.livestream_apd.domain.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    
    private Long id;
    private UserSummaryResponse user;
    private Long chatRoomId;
    private Long parentMessageId;
    private String content;
    private ChatMessage.MessageType messageType;
    private Map<String, String> metadata;
    private Map<String, Integer> reactions;
    private Boolean isDeleted;
    private Boolean isEdited;
    private LocalDateTime editedAt;
    private LocalDateTime createdAt;
}
