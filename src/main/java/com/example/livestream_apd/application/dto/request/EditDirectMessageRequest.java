package com.example.livestream_apd.application.dto.request;

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
public class EditDirectMessageRequest {
    
    @NotNull(message = "Message ID không được null")
    private Long messageId;
    
    @NotBlank(message = "Nội dung tin nhắn không được trống")
    @Size(max = 4000, message = "Nội dung tin nhắn không được quá 4000 ký tự")
    private String content;
}
