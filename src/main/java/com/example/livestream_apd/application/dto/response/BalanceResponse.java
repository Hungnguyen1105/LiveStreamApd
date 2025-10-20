package com.example.livestream_apd.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private BigDecimal currentBalance;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal pendingAmount;

    private Long totalTransactions;

    @Builder.Default
    private String currency = "VND";
}
