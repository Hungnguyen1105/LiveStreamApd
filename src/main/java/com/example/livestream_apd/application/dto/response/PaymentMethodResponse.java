package com.example.livestream_apd.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {

    private List<PaymentMethodInfo> availableMethods;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodInfo {
        private String code;
        private String name;
        private String description;
        private Boolean isActive;
        private String iconUrl;
        private List<String> supportedCurrencies;
    }
}
