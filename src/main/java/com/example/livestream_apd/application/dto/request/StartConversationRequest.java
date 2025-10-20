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
public class StartConversationRequest {
    
    @NotNull(message = "User ID không được null")
    private Long targetUserId;
    
    private String initialMessage;
}
