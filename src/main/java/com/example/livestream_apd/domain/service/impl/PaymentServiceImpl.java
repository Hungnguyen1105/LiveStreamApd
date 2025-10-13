package com.example.livestream_apd.domain.service.impl;

import com.example.livestream_apd.application.dto.response.*;
import com.example.livestream_apd.application.dto.request.*;
import com.example.livestream_apd.domain.entity.Transaction;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.TransactionRepository;
import com.example.livestream_apd.domain.repository.UserRepository;
import com.example.livestream_apd.domain.service.PaymentService;
import com.example.livestream_apd.domain.service.VNPayService;
import com.example.livestream_apd.domain.service.EmailService;
import com.example.livestream_apd.domain.service.OtpService;
import com.example.livestream_apd.utils.exceptions.ResourceForbiddenException;
import com.example.livestream_apd.utils.exceptions.ResourceNotFoundException;
import com.example.livestream_apd.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final VNPayService vnPayService;
    private final EmailService emailService;
    private final OtpService otpService;

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(User currentUser) {
        BigDecimal totalIncome = transactionRepository.getTotalCreditAmount(currentUser);
        BigDecimal totalExpense = transactionRepository.getTotalDebitAmount(currentUser);
        long pendingTransactions = transactionRepository.countPendingTransactionsByUser(currentUser);
        long totalTransactions = transactionRepository.count();

        return BalanceResponse.builder()
                .currentBalance(currentUser.getBalance())
                .totalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .totalExpense(totalExpense != null ? totalExpense : BigDecimal.ZERO)
                .pendingAmount(BigDecimal.ZERO) // Calculate from pending transactions if needed
                .totalTransactions(totalTransactions)
                .build();
    }

    @Override
    @Transactional
    public PaymentUrlResponse createTopupPayment(User currentUser, TopupRequest request, String ipAddress) {
        // Generate transaction ID
        String transactionId = vnPayService.generateTransactionId();
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(currentUser)
                .transactionId(transactionId)
                .type(Transaction.TransactionType.TOPUP)
                .amount(request.getAmount())
                .status(Transaction.TransactionStatus.PENDING)
                .paymentMethod("VNPAY")
                .description(request.getDescription())
                .build();
        
        transactionRepository.save(transaction);
        
        // Create VNPay payment URL
        String orderInfo = "Nap tien tai khoan - " + currentUser.getUsername();
        String paymentUrl = vnPayService.createPaymentUrl(
                transactionId, 
                request.getAmount().longValue(), 
                orderInfo, 
                ipAddress,
                request.getReturnUrl()
        );
        
        return PaymentUrlResponse.builder()
                .paymentUrl(paymentUrl)
                .transactionId(transactionId)
                .message("Chuyển hướng đến cổng thanh toán VNPay")
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse processWithdraw(User currentUser, WithdrawRequest request) {
        // Check if user has enough balance
        if (currentUser.getBalance().compareTo(request.getAmount()) < 0) {
            throw new ResourceForbiddenException("Số dư không đủ để thực hiện giao dịch");
        }

        // Generate transaction ID
        String transactionId = vnPayService.generateTransactionId();
        
        // Generate OTP
        String otpCode = otpService.generateOtp();
        LocalDateTime otpExpiry = TimeUtil.nowUtcPlusMinutes(5);

        // Create withdrawal transaction
        Map<String, String> metadata = new HashMap<>();
        metadata.put("accountNumber", request.getBankAccount());
        metadata.put("bankName", request.getBankName());
        metadata.put("accountHolderName", request.getAccountHolder());
        metadata.put("bankCode", request.getBankCode());

        Transaction transaction = Transaction.builder()
                .user(currentUser)
                .transactionId(transactionId)
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .status(Transaction.TransactionStatus.PENDING)
                .paymentMethod("BANK_TRANSFER")
                .description(request.getDescription())
                .metadata(metadata)
                .otpCode(otpCode)
                .otpExpiresAt(otpExpiry)
                .build();

        // Don't update user balance yet - wait for OTP verification
        // Balance will be updated when OTP is verified successfully
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Send OTP email
        try {
            emailService.sendWithdrawOTP(
                currentUser.getEmail(), 
                currentUser.getFullName(), 
                otpCode, 
                request.getAmount().toString()
            );
            log.info("OTP email sent for withdrawal transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to send OTP email for transaction: {}", transactionId, e);
            // Don't fail the transaction if email fails
        }
        return mapToTransactionResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(User currentUser, TransactionFilterRequest filter, Pageable pageable) {
        Page<Transaction> transactions;

        if (filter.getType() != null && filter.getStatus() != null) {
            Transaction.TransactionType type = Transaction.TransactionType.valueOf(filter.getType().toUpperCase());
            Transaction.TransactionStatus status = Transaction.TransactionStatus.valueOf(filter.getStatus().toUpperCase());
            transactions = transactionRepository.findByUserAndTypeAndStatusOrderByCreatedAtDesc(currentUser, type, status, pageable);
        } else if (filter.getType() != null) {
            Transaction.TransactionType type = Transaction.TransactionType.valueOf(filter.getType().toUpperCase());
            transactions = transactionRepository.findByUserAndTypeOrderByCreatedAtDesc(currentUser, type, pageable);
        } else if (filter.getStatus() != null) {
            Transaction.TransactionStatus status = Transaction.TransactionStatus.valueOf(filter.getStatus().toUpperCase());
            transactions = transactionRepository.findByUserAndStatusOrderByCreatedAtDesc(currentUser, status, pageable);
        } else {
            transactions = transactionRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
        }

        return transactions.map(this::mapToTransactionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatisticsResponse getPaymentStatistics(User currentUser, String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);

        // Get statistics for each transaction type
        Map<String, BigDecimal> incomeByType = new HashMap<>();
        Map<String, BigDecimal> expenseByType = new HashMap<>();

        for (Transaction.TransactionType type : Transaction.TransactionType.values()) {
            BigDecimal amount = transactionRepository.sumAmountByUserAndTypeAndDateRange(currentUser, type, start, end);
            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                if (type == Transaction.TransactionType.TOPUP || 
                    type == Transaction.TransactionType.GIFT_INCOME || 
                    type == Transaction.TransactionType.COMMISSION) {
                    incomeByType.put(type.name(), amount);
                } else {
                    expenseByType.put(type.name(), amount);
                }
            }
        }

        BigDecimal totalIncome = incomeByType.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenseByType.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentStatisticsResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netIncome(totalIncome.subtract(totalExpense))
                .incomeByType(incomeByType)
                .expenseByType(expenseByType)
                .build();
    }

    @Override
    @Transactional
    public String processVNPayCallback(Map<String, String> params) {
        try {
            // Validate signature
            if (!vnPayService.validateSignature(params)) {
                log.error("Invalid VNPay signature");
                return "INVALID_SIGNATURE";
            }

            String transactionId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String vnpayTransactionId = params.get("vnp_TransactionNo");

            Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

            if ("00".equals(responseCode)) {
                // Payment successful
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setVnpayTransactionId(vnpayTransactionId);
                
                // Update user balance for topup
                if (transaction.getType() == Transaction.TransactionType.TOPUP) {
                    User user = transaction.getUser();
                    user.setBalance(user.getBalance().add(transaction.getAmount()));
                    userRepository.save(user);
                }
            } else {
                // Payment failed
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
            }

            transaction.setGatewayResponse(params.toString());
            transactionRepository.save(transaction);

            return "SUCCESS";

        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            return "ERROR";
        }
    }

    @Override
    public PaymentMethodResponse getPaymentMethods() {
        List<PaymentMethodResponse.PaymentMethodInfo> methods = new ArrayList<>();
        
        PaymentMethodResponse.PaymentMethodInfo vnpay = PaymentMethodResponse.PaymentMethodInfo.builder()
                .code("VNPAY")
                .name("VNPay")
                .description("Thanh toán qua cổng VNPay")
                .isActive(true)
                .iconUrl("/images/vnpay-logo.png")
                .supportedCurrencies(Arrays.asList("VND"))
                .build();
        
        methods.add(vnpay);

        return PaymentMethodResponse.builder()
                .availableMethods(methods)
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse verifyTransaction(User currentUser, VerifyTransactionRequest request) {
        Transaction transaction = transactionRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", request.getTransactionId()));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Không có quyền truy cập giao dịch này");
        }

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new ResourceForbiddenException("Giao dịch này đã được xử lý");
        }

        // For withdrawal transactions, verify OTP and process
        if (transaction.getType() == Transaction.TransactionType.WITHDRAWAL) {
            // Validate OTP
            if (!otpService.validateOtpWithExpiry(request.getOtpCode(), transaction.getOtpCode(), transaction.getOtpExpiresAt())) {
                throw new ResourceForbiddenException("Mã OTP không hợp lệ hoặc đã hết hạn");
            }

            // Check if user still has enough balance
            if (currentUser.getBalance().compareTo(transaction.getAmount()) < 0) {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transaction.setGatewayResponse("Insufficient balance at verification time");
                transactionRepository.save(transaction);
                throw new ResourceForbiddenException("Số dư không đủ để thực hiện giao dịch");
            }

            // OTP verified successfully - complete the withdrawal
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setGatewayResponse("OTP verified successfully");
            
            // Deduct balance now
            currentUser.setBalance(currentUser.getBalance().subtract(transaction.getAmount()));
            userRepository.save(currentUser);
            
            transactionRepository.save(transaction);
            
            log.info("Withdrawal transaction {} completed successfully via OTP", transaction.getTransactionId());
            return mapToTransactionResponse(transaction);
        }

        // For other transaction types, just return details
        return mapToTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse processWithdrawCallback(String transactionId, String status, String bankTransactionId, String note) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getType() != Transaction.TransactionType.WITHDRAWAL) {
            throw new ResourceForbiddenException("Giao dịch này không phải là giao dịch rút tiền");
        }

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new ResourceForbiddenException("Giao dịch này đã được xử lý");
        }

        if ("SUCCESS".equals(status)) {
            // Bank transfer successful
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setReferenceId(bankTransactionId);
            if (note != null) {
                transaction.setGatewayResponse("Bank transfer completed. Note: " + note);
            }
            log.info("Withdraw transaction {} completed successfully", transactionId);
        } else if ("FAILED".equals(status)) {
            // Bank transfer failed - refund user balance
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            if (note != null) {
                transaction.setGatewayResponse("Bank transfer failed. Note: " + note);
            }
            
            // Refund user balance
            User user = transaction.getUser();
            user.setBalance(user.getBalance().add(transaction.getAmount()));
            userRepository.save(user);
            
            log.info("Withdraw transaction {} failed, balance refunded", transactionId);
        } else {
            throw new ResourceForbiddenException("Status không hợp lệ. Chỉ chấp nhận SUCCESS hoặc FAILED");
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToTransactionResponse(savedTransaction);
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .type(transaction.getType().name())
                .typeDisplayName(transaction.getType().getDisplayName())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .statusDisplayName(transaction.getStatus().getDisplayName())
                .paymentMethod(transaction.getPaymentMethod())
                .vnpayTransactionId(transaction.getVnpayTransactionId())
                .metadata(null) // Skip metadata to avoid lazy loading issue
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .isCredit(transaction.isCredit())
                .isDebit(transaction.isDebit())
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse processGiftPayment(Long senderId, Long receiverId, Double amount, String description) {
        // Find sender and receiver
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        BigDecimal giftAmount = BigDecimal.valueOf(amount);

        // Check if sender has sufficient balance
        if (sender.getBalance().compareTo(giftAmount) < 0) {
            throw new ResourceForbiddenException("Insufficient balance to send gift");
        }

        try {
            // Deduct from sender
            sender.setBalance(sender.getBalance().subtract(giftAmount));
            userRepository.save(sender);

            // Add to receiver (80% of gift value, 20% platform fee)
            BigDecimal receiverAmount = giftAmount.multiply(BigDecimal.valueOf(0.8));
            receiver.setBalance(receiver.getBalance().add(receiverAmount));
            userRepository.save(receiver);

            // Create sender transaction (debit)
            Transaction senderTransaction = Transaction.builder()
                    .user(sender)
                    .amount(giftAmount)
                    .type(Transaction.TransactionType.GIFT_SENT)
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .description(description)
                    .paymentMethod("GIFT")
                    .referenceId("GIFT_" + System.currentTimeMillis())
                    .build();

            transactionRepository.save(senderTransaction);

            // Create receiver transaction (credit)
            Transaction receiverTransaction = Transaction.builder()
                    .user(receiver)
                    .amount(receiverAmount)
                    .type(Transaction.TransactionType.GIFT_RECEIVED)
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .description("Gift received: " + description)
                    .paymentMethod("GIFT")
                    .referenceId("GIFT_RCV_" + System.currentTimeMillis())
                    .build();

            transactionRepository.save(receiverTransaction);

            log.info("Gift payment processed: {} sent {} to {}", 
                    sender.getUsername(), giftAmount, receiver.getUsername());

            return mapToTransactionResponse(senderTransaction);

        } catch (Exception e) {
            log.error("Failed to process gift payment: ", e);
            throw new RuntimeException("Failed to process gift payment: " + e.getMessage());
        }
    }
}
