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
public class VerifyTransactionRequest {

    @NotBlank(message = "Transaction ID không được để trống")
    private String transactionId;

    @NotBlank(message = "OTP không được để trống")
    private String otpCode;
}
