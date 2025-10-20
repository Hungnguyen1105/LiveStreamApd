package com.example.livestream_apd.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatisticsResponse {

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netIncome;

    private Long totalTransactions;

    private Long totalTopups;

    private Long totalWithdrawals;

    private BigDecimal averageTransactionAmount;

    private List<DailyStatistic> dailyStatistics;

    private List<MonthlyStatistic> monthlyStatistics;

    private Map<String, BigDecimal> incomeByType;

    private Map<String, BigDecimal> expenseByType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStatistic {
        private LocalDate date;
        private BigDecimal income;
        private BigDecimal expense;
        private Long transactionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStatistic {
        private Integer year;
        private Integer month;
        private BigDecimal income;
        private BigDecimal expense;
        private Long transactionCount;
    }
}
