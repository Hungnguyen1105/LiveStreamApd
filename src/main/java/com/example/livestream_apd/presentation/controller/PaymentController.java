package com.example.livestream_apd.presentation.controller;

import com.example.livestream_apd.application.dto.response.*;
import com.example.livestream_apd.application.dto.request.*;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.UserRepository;
import com.example.livestream_apd.domain.service.PaymentService;
import com.example.livestream_apd.infrastructure.security.UserDetailsServiceImpl;
import com.example.livestream_apd.utils.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment & Wallet", description = "API quản lý thanh toán và ví điện tử")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @GetMapping("/balance")
    @Operation(summary = "Lấy thông tin số dư tài khoản", description = "Trả về thông tin số dư hiện tại và thống kê tổng quan")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @CurrentUser User currentUser) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        BalanceResponse balance = paymentService.getBalance(currentUser);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    @PostMapping("/topup")
    @Operation(summary = "Nạp tiền vào tài khoản", description = "Tạo yêu cầu nạp tiền thông qua VNPay")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> topup(
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid @RequestBody TopupRequest request,
            HttpServletRequest httpRequest) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String ipAddress = getClientIpAddress(httpRequest);
        PaymentUrlResponse response = paymentService.createTopupPayment(currentUser, request, ipAddress);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Rút tiền từ tài khoản", description = "Tạo yêu cầu rút tiền về tài khoản ngân hàng")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid @RequestBody WithdrawRequest request) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TransactionResponse response = paymentService.processWithdraw(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Lấy lịch sử giao dịch", description = "Trả về danh sách giao dịch với khả năng lọc và phân trang")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<TransactionResponse>> getTransactions(
            @CurrentUser User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String paymentMethod) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PagedResponse.<TransactionResponse>builder()
                            .content(null)
                            .page(0)
                            .size(0)
                            .totalElements(0)
                            .totalPages(0)
                            .first(true)
                            .last(true)
                            .build());
        }

        TransactionFilterRequest filter = TransactionFilterRequest.builder()
                .type(type)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .paymentMethod(paymentMethod)
                .build();

        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionResponse> transactions = paymentService.getTransactionHistory(currentUser, filter, pageable);

        PagedResponse<TransactionResponse> response = PagedResponse.<TransactionResponse>builder()
                .content(transactions.getContent())
                .page(transactions.getNumber())
                .size(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .first(transactions.isFirst())
                .last(transactions.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Thống kê thu chi", description = "Trả về thống kê thu chi trong khoảng thời gian")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PaymentStatisticsResponse>> getStatistics(
            @CurrentUser User currentUser,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-12-31") String endDate) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        PaymentStatisticsResponse statistics = paymentService.getPaymentStatistics(currentUser, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @RequestMapping(value = "/vnpay/callback", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "VNPay callback", description = "Xử lý callback từ VNPay sau khi thanh toán")
    public ResponseEntity<String> vnpayCallback(@RequestParam Map<String, String> params) {
        log.info("VNPay callback received with params: {}", params);
        
        String result = paymentService.processVNPayCallback(params);
        
        if ("SUCCESS".equals(result)) {
            log.info("VNPay callback processed successfully for transaction: {}", params.get("vnp_TxnRef"));
            return ResponseEntity.ok("SUCCESS");
        } else {
            log.error("VNPay callback failed for transaction: {}", params.get("vnp_TxnRef"));
            return ResponseEntity.badRequest().body("FAILED");
        }
    }

    @GetMapping("/methods")
    @Operation(summary = "Lấy danh sách phương thức thanh toán", description = "Trả về các phương thức thanh toán khả dụng")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getPaymentMethods() {
        PaymentMethodResponse methods = paymentService.getPaymentMethods();
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @PostMapping("/verify")
    @Operation(summary = "Xác thực giao dịch", description = "Xác thực giao dịch bằng OTP")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TransactionResponse>> verifyTransaction(
            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
            @Valid @RequestBody VerifyTransactionRequest request) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TransactionResponse response = paymentService.verifyTransaction(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/withdraw/callback")
    @Operation(summary = "Bank transfer callback", description = "Xử lý callback từ ngân hàng sau khi chuyển tiền thành công")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')") // Temporarily disabled for testing
    public ResponseEntity<ApiResponse<TransactionResponse>> withdrawCallback(
            @RequestParam String transactionId,
            @RequestParam String status,
            @RequestParam(required = false) String bankTransactionId,
            @RequestParam(required = false) String note) {
        
        TransactionResponse response = paymentService.processWithdrawCallback(transactionId, status, bankTransactionId, note);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

//    @GetMapping("/statistics")
//    @Operation(summary = "Thống kê thu chi", description = "Lấy thống kê thu chi theo khoảng thời gian")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<ApiResponse<PaymentStatisticsResponse>> getPaymentStatistics(
//            @AuthenticationPrincipal UserDetailsServiceImpl.UserPrincipal userPrincipal,
//            @RequestParam(required = false) String startDate,
//            @RequestParam(required = false) String endDate) {
//
//        if (userPrincipal == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResponse.error("Unauthorized"));
//        }
//
//        User currentUser = userRepository.findById(userPrincipal.getId())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        PaymentStatisticsResponse statistics = paymentService.getPaymentStatistics(currentUser, startDate, endDate);
//        return ResponseEntity.ok(ApiResponse.success(statistics));
//    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
