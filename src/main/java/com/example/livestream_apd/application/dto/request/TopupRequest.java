package com.example.livestream_apd.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupRequest {

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "10000", message = "Số tiền nạp tối thiểu là 10,000 VND")
    private BigDecimal amount;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

    private String returnUrl;

    private String cancelUrl;
}
