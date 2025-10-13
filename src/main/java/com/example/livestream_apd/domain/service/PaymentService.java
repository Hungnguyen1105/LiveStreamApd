package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.application.dto.response.*;
import com.example.livestream_apd.application.dto.request.*;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PaymentService {
    
    BalanceResponse getBalance(User currentUser);
    
    PaymentUrlResponse createTopupPayment(User currentUser, TopupRequest request, String ipAddress);
    
    TransactionResponse processWithdraw(User currentUser, WithdrawRequest request);
    
    Page<TransactionResponse> getTransactionHistory(User currentUser, TransactionFilterRequest filter, Pageable pageable);
    
    PaymentStatisticsResponse getPaymentStatistics(User currentUser, String startDate, String endDate);
    
    String processVNPayCallback(Map<String, String> params);
    
    PaymentMethodResponse getPaymentMethods();
    
    TransactionResponse verifyTransaction(User currentUser, VerifyTransactionRequest request);
    
    TransactionResponse processWithdrawCallback(String transactionId, String status, String bankTransactionId, String note);
    
    // LiveStream gift payment
    TransactionResponse processGiftPayment(Long senderId, Long receiverId, Double amount, String description);
}
