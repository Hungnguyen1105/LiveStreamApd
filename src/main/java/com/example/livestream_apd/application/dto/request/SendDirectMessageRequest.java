package com.example.livestream_apd.application.dto.request;

import com.example.livestream_apd.domain.entity.DirectMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendDirectMessageRequest {
    
    @NotNull(message = "Conversation ID không được null")
    private Long conversationId;
    
    @NotBlank(message = "Nội dung tin nhắn không được trống")
    @Size(max = 4000, message = "Nội dung tin nhắn không được quá 4000 ký tự")
    private String content;
    
    @Builder.Default
    private DirectMessage.MessageType messageType = DirectMessage.MessageType.TEXT;
    
    private String mediaUrl;
    
    private String thumbnailUrl;
    
    private Long mediaSize;
    
    private String mediaType;
    
    private Long replyToMessageId;
}
