package com.example.livestream_apd.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorRequest {

    @NotNull(message = "Chat room ID is required")
    private Long chatRoomId;

    @Builder.Default
    private Boolean isTyping = true;
}
