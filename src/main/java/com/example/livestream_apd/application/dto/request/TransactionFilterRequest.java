package com.example.livestream_apd.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterRequest {

    private String type; // TransactionType as string

    private String status; // TransactionStatus as string

    private String startDate; // ISO format date string

    private String endDate; // ISO format date string

    private String paymentMethod;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "desc";
}
