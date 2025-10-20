package com.example.livestream_apd.application.dto.request;

import com.example.livestream_apd.domain.entity.Post;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CreatePostRequest {
    @Size(max = 5000, message = "Caption không vượt quá 5000 kí tự")
    private String caption;

    @Size(max = 10, message = "số lượng media tối đa 10")
    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();

    private String location;

    @Size(max = 20, message = "số lượng tags tối đa 20")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private boolean isPublic = true;

    @AssertTrue(message = "Post phải có caption hoặc media")
    public boolean isValidPost() {
        return (caption != null && !caption.trim().isEmpty()) || 
               (mediaUrls != null && !mediaUrls.isEmpty());
    }
}
