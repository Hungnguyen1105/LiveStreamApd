package com.example.livestream_apd.application.dto.request;

import jakarta.validation.constraints.Size;
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

public class UpdatePostRequest {
    @Size(max = 5000, message = "Caption không vượt quá 5000 kí tự")
    private String caption;

    @Size(min = 1, max = 10, message = "số lương media 1-10")
    private List<String> mediaUrls = new ArrayList<>();

    private String location;

    @Size(max = 20, message = "số lương media 1-10")

    private List<String> tags = new ArrayList<>();

    private Boolean isPublic;
}
