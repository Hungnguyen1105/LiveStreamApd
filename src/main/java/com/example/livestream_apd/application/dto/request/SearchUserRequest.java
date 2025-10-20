package com.example.livestream_apd.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SearchUserRequest {
    @NotBlank(message = "từ khoá tìm kiếm không được để trống")
    @Size(min = 1, max = 100, message = "từ khoá timf kiếm từ 1-100")
    private String query;
    private Integer page = 0;
    private Integer size = 10;
}
