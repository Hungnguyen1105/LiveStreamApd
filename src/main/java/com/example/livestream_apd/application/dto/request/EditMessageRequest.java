package com.example.livestream_apd.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditMessageRequest {

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String content;
}
