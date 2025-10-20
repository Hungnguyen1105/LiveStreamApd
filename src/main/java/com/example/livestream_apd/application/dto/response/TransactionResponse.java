package com.example.livestream_apd.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;

    private String transactionId;

    private String type;

    private String typeDisplayName;

    private BigDecimal amount;

    private String status;

    private String statusDisplayName;

    private String paymentMethod;

    private String vnpayTransactionId;

    private Map<String, String> metadata;

    private String description;

    private String referenceId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isCredit;

    private Boolean isDebit;
}
