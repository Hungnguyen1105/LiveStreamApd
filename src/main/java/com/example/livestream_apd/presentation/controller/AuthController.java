package com.example.livestream_apd.presentation.controller;

import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.AuthResponse;
import com.example.livestream_apd.application.dto.response.UserResponse;
import com.example.livestream_apd.application.dto.request.*;
import com.example.livestream_apd.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name ="Authentication", description = "API xac thuc nguoi dung")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng kí tài khoản", description = "cam on da dki")

    public ResponseEntity <ApiResponse <UserResponse>> register (@Valid @RequestBody RegisterRequest request ,
                                                                 HttpServletRequest httpServletRequest) {
        String ipAddress = getClientIpAddress(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");
        ApiResponse<UserResponse> response = authService.register(request,ipAddress,userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng Nhập" , description = "Dang nhap di")
    public ResponseEntity<ApiResponse<AuthResponse>> login (@Valid @RequestBody LoginRequest request ,
                                                            HttpServletRequest httpServletRequest) {
        String ipAddress = getClientIpAddress(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");
        ApiResponse<AuthResponse> response = authService.login(request,ipAddress,userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Xác thực email", description = "Xác thực email bằng token và OTP")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        ApiResponse<String> response = authService.verifyEmail(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi email đặt lại mật khẩu")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        ApiResponse<String> response = authService.forgotPassword(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest
    ){
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        ApiResponse<String> response = authService.resetPassword(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Gửi lại email xác thực", description = "")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request, HttpServletRequest httpRequest
    ){
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        ApiResponse<String> response = authService.resendEmailVerification(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary =  "Đăng xuất", description = "")
    public ResponseEntity<ApiResponse<String>> logout(
            @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest
    ){
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        ApiResponse<String> response = authService.logout(request.getRefreshToken(),ipAddress,userAgent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Kiểm tra trạng thái hệ thống", description = "")
    public ResponseEntity<ApiResponse<String>> health(){
        return ResponseEntity.ok(ApiResponse.success("AUTHENTICATION_SERVICE đang chạy"));
    }


    private String getClientIpAddress(HttpServletRequest httpServletRequest) {
        String xForwarderFor = httpServletRequest.getHeader("X-Forwarded-For"); // header cua cac may
        if(xForwarderFor != null && !xForwarderFor.isEmpty()){
            return xForwarderFor.split(",")[0].trim();
        }
        String xRealIp = httpServletRequest.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return httpServletRequest.getRemoteAddr();
    }
}
