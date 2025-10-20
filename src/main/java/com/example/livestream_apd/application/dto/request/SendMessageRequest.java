package com.example.livestream_apd.application.dto.request;

import com.example.livestream_apd.domain.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "Chat room ID is required")
    private Long chatRoomId;

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String content;

    @Builder.Default
    private ChatMessage.MessageType messageType = ChatMessage.MessageType.TEXT;

    private Long parentMessageId;

    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
}
