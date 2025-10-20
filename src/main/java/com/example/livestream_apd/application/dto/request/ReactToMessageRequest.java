package com.example.livestream_apd.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactToMessageRequest {

    @NotBlank(message = "Reaction type is required")
    private String reactionType; // e.g., "like", "love", "laugh", "angry", etc.
}
