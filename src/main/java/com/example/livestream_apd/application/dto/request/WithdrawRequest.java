package com.example.livestream_apd.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class WithdrawRequest {

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "50000", message = "Số tiền rút tối thiểu là 50,000 VND")
    private BigDecimal amount;

    @NotBlank(message = "Số tài khoản không được để trống")
    @Size(min = 6, max = 20, message = "Số tài khoản phải từ 6-20 ký tự")
    private String bankAccount;

    @NotBlank(message = "Tên ngân hàng không được để trống")
    @Size(max = 100, message = "Tên ngân hàng không được vượt quá 100 ký tự")
    private String bankName;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    @Size(max = 100, message = "Tên chủ tài khoản không được vượt quá 100 ký tự")
    private String accountHolder;

    @Size(max = 10, message = "Mã ngân hàng không được vượt quá 10 ký tự")
    private String bankCode;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;
}
