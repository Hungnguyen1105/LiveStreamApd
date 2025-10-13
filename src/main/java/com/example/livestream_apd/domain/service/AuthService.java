package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.AuthResponse;
import com.example.livestream_apd.application.dto.response.UserResponse;
import com.example.livestream_apd.application.dto.request.*;
import com.example.livestream_apd.domain.entity.*;
import com.example.livestream_apd.domain.repository.*;

import com.example.livestream_apd.infrastructure.security.JwtTokenProvider;
import com.example.livestream_apd.utils.TimeUtil;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional

public class AuthService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;

    @Value("300000")
    private long otpExpiration;

    @Value("3")
    private int maxOtpAttempts;

    @Value("60000")
    private long resendCooldown;

    public ApiResponse <UserResponse> register(RegisterRequest request, String ipAddress, String userAgent ) {
        try{
            if (userRepository.existsByEmail(request.getEmail())) {
                return ApiResponse.error("Email đã tồn taị");
            }
            if(userRepository.existsByUsername(request.getUsername())) {
                return ApiResponse.error("Tên đăng nhập đã tồn tại");
            }
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .bio(request.getBio())
                    .status(User.UserStatus.PENDING_VERIFICATION)
                    .isEmailVerified(false)
                    .build();
            Role defaultRole = roleRepository.findByName(Role.RoleName.USER).orElseThrow(()
                    -> new RuntimeException("Default user role not found"));
            user.addRole(defaultRole);
            user = userRepository.save(user);
            String token = UUID.randomUUID().toString();
            String otpCode = otpService.generateOtp();


            EmailVerificationToken verificationToken = EmailVerificationToken
                    .builder()
                    .user(user)
                    .token(token)
                    .otpCode(otpCode)
                    .email(request.getEmail())
                    .attempts(maxOtpAttempts)
                    .attempts(0)
                    .expiresAt(TimeUtil.nowUtcPlusSeconds(otpExpiration / 1000))
                    .build();
            emailVerificationTokenRepository.save(verificationToken);
            emailService.sendEmailVerificationOtp(request.getEmail(),request.getFullName(),otpCode,token);
            auditLogRepository.save(AuditLog.registrationSuccess(user,ipAddress,userAgent));
            UserResponse userResponse = mapToUserResponse(user);
            return ApiResponse.success("Đăng kí thành công vui lòng kiểm tra email để xác thực tài khoản", userResponse);

        }catch (Exception e){
            log.error("Đăng kí thất bại với email : {}" , request.getEmail(),e);
            return ApiResponse.error("Đăng ký thất bại " + e.getMessage());
        }
    }

    public ApiResponse<AuthResponse> login (LoginRequest request, String ipAddress, String userAgent) {
        try{
            Optional<User> userOptional = userRepository.findByEmailOrUsername(request.getEmailOrUsername(), request.getEmailOrUsername());
            if(userOptional.isEmpty()){
                auditLogRepository.save(AuditLog.loginFailure(request.getEmailOrUsername(),ipAddress,userAgent,"User not found"));
                return ApiResponse.error("Tên đăng nhập và mật khẩu không đúng");
            }
            User user = userOptional.get();
            if (!user.canLogin()){
                String reason = user.getStatus() == User.UserStatus.BANNED?"Account Banned" : "Email not verified";
                auditLogRepository.save(AuditLog.loginFailure(request.getEmailOrUsername(),ipAddress,userAgent,reason));
                if (user.getStatus() == User.UserStatus.BANNED){
                    return ApiResponse.error("Tài Khoản bị khoá");
                } else {
                    return ApiResponse.error("Vui lòng xác thực email trước khi đăng nhập");
                }
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken (user.getEmail(),request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

            UserSession session = UserSession.builder()
                    .user(user)
                    .sessionToken(accessToken)
                    .refreshToken(refreshToken)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .expiresAt(TimeUtil.nowUtcPlusSeconds(jwtTokenProvider.getRefreshTokenExpiration()/1000))
                    .lastActivity(TimeUtil.nowUtc())
                    .build();
            userSessionRepository.save(session);

            user.updateLastSeen();
            userRepository.save(user);

            auditLogRepository.save(AuditLog.loginSuccess(user, ipAddress, userAgent));
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expireAt(TimeUtil.nowUtcPlusSeconds(jwtTokenProvider.getAccessTokenExpiration()/1000))
                    .user(mapToUserResponse(user))
                    .build();
            return ApiResponse.success("Đăng nhập thành công" ,authResponse);
        } catch (Exception e){
            log.error("Đăng nhập thất bại vì {}", request.getEmailOrUsername(),e);
            auditLogRepository.save(AuditLog.loginFailure(request.getEmailOrUsername(),ipAddress,userAgent, e.getMessage()));
            return ApiResponse.error("Đăng nhập thất bại" + e.getMessage());
        }
    }

    public ApiResponse<String> verifyEmail(VerifyEmailRequest request, String ipAddress, String userAgent) {
        try {
            Optional<EmailVerificationToken> tokenOpt = emailVerificationTokenRepository.findByToken(request.getToken());

            if (tokenOpt.isEmpty()) {
                return ApiResponse.error("Token xác thực không hợp lệ");
            }

            EmailVerificationToken token = tokenOpt.get();

            if (!token.isValid()) {
                return ApiResponse.error("Token đã hết hạn hoặc đã được sử dụng");
            }

            if (!token.canAttempt()) {
                return ApiResponse.error("Đã vượt quá số lần thử tối đa");
            }

            token.incrementAttempts();

            if (!otpService.validateOtp(request.getOtpCode(), token.getOtpCode())) {
                emailVerificationTokenRepository.save(token);
                return ApiResponse.error("Mã OTP không đúng");
            }

            // Mark token as used
            token.markAsUsed();
            emailVerificationTokenRepository.save(token);

            // Activate user
            User user = token.getUser();
            user.activate();
            userRepository.save(user);

            // Invalidate all other verification tokens for this user
            emailVerificationTokenRepository.invalidateAllUserTokens(user);

            // Send welcome email
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

            // Log successful verification
            auditLogRepository.save(AuditLog.emailVerificationSuccess(user, ipAddress, userAgent));

            return ApiResponse.success("Xác thực email thành công! Tài khoản đã được kích hoạt.");

        } catch (Exception e) {
            log.error("Email verification failed for token: {}", request.getToken(), e);
            return ApiResponse.error("Xác thực email thất bại: " + e.getMessage());
        }
    }

    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request, String ipAddress, String userAgent) {
        try{
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                return ApiResponse.success("Nếu email tồn tại chung tôi sẽ gửi hướng dẫn đặt lại mật khẩu");
            }
            User user = userOptional.get();
            passwordResetTokenRepository.invalidateAllUserTokens(user);
            String token = UUID.randomUUID().toString();
            String otpCode = otpService.generateOtp();
            PasswordResetToken passwordResetToken = PasswordResetToken
                    .builder()
                    .user(user)
                    .token(token)
                    .otpCode(otpCode)
                    .email(request.getEmail())
                    .maxAttempts(maxOtpAttempts)
                    .expiresAt(TimeUtil.nowUtcPlusSeconds(otpExpiration/1000))
                    .build();
            passwordResetTokenRepository.save(passwordResetToken);
            emailService.sendResetPasswordEmail(
                    request.getEmail(),
                    user.getFullName(),
                    otpCode,
                    token
            );
            auditLogRepository.save(AuditLog.passwordResetRequest(
                    user,
                    ipAddress,
                    userAgent
            ));
            return ApiResponse.success("Nếu email tồn tại chúng tôi sẽ gửi hướng dẫn đặt lại mật khẩu :");
        }catch (Exception e){
            log.error("Quên mật khẩu thất bại cho: {}", request.getEmail(),e);
            return ApiResponse.error("Có lỗi vui lòng thử lại sau");
        }
    }

    public ApiResponse<String> resetPassword(ResetPasswordRequest request, String ipAddress, String userAgent) {
        try{
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(request.getToken());
            if (tokenOpt.isEmpty()) {
                return ApiResponse.error("token đặt lại mật khẩu không hợp lệ");
            }
            PasswordResetToken token = tokenOpt.get();
            if (!token.isValid()) {
                return ApiResponse.error("token hết hạn hoặc đã sử dụng");
            }
            if (!token.canAttempt()){
                return ApiResponse.error("Đã vượt qua số lần thử");
            }
            token.incrementAttempts();

            if (!otpService.validateOtp(request.getOtpCode(), token.getOtpCode())){
                passwordResetTokenRepository.save(token);
                return ApiResponse.error("Otp không đúng");
            }

            token.markAsUsed();
            passwordResetTokenRepository.save(token);

            User user = token.getUser();
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            userSessionRepository.invalidateAllUserSessions(user);
            passwordResetTokenRepository.invalidateAllUserTokens(user);
            emailService.sendPasswordChangeConfirmation(user.getEmail(), user.getFullName());
            auditLogRepository.save(AuditLog.passwordResetSuccess(user, ipAddress, userAgent));
            return ApiResponse.success("Đặt lại mật khẩu thành công, vui lòng đăng nhập lại");

        }catch (Exception e){
            log.error("Lỗi đặt lại mật khẩu {}", request.getToken(), e);
            return ApiResponse.error("Đặt lại mật khẩu thất bại" + e.getMessage());

        }
    }

    public ApiResponse<String> resendEmailVerification(ResendVerificationRequest request, String ipAddress, String userAgent) {
        try{
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ApiResponse.error("Email không tồn tại trong hệ thống ");
            }

            User user = userOpt.get();

            if (user.getIsEmailVerified()){
                return ApiResponse.error("Nguời dùng được xác thực");
            }
            if(user.getStatus() == User.UserStatus.BANNED){
                return ApiResponse.error("Tài khoản đã bị khoá do vi phạm");
            }

            Optional<EmailVerificationToken> lastTokenOpt = emailVerificationTokenRepository
                    .findTopByUserOrderByCreatedAtDesc(user);
            if(lastTokenOpt.isPresent()){
                EmailVerificationToken token = lastTokenOpt.get();
                long timeSinceLastSend = java.time.Duration.between(token.getCreatedAt()
                        , TimeUtil.nowUtc()).toMillis();
                if (timeSinceLastSend < resendCooldown){
                    long remainingTime = (resendCooldown - timeSinceLastSend) / 1000;
                    return ApiResponse.error("Vui lòng đơị " + remainingTime + "s trước khi gửi lại");
                }
            }
            emailVerificationTokenRepository.invalidateAllUserTokens(user);
            String token = UUID.randomUUID().toString();
            String otpCode = otpService.generateOtp();

            EmailVerificationToken emailVerificationToken = EmailVerificationToken
                    .builder()
                    .user(user)
                    .token(token)
                    .otpCode(otpCode)
                    .email(request.getEmail())
                    .maxAttempts(maxOtpAttempts)
                    .expiresAt(TimeUtil.nowUtcPlusSeconds(otpExpiration/1000))
                    .build();
            emailVerificationTokenRepository.save(emailVerificationToken);

            emailService.sendEmailVerificationOtp(
                    request.getEmail(),
                    user.getFullName(),
                    otpCode,
                    token
            );
            auditLogRepository.save(AuditLog
                    .builder()
                    .user(user)
                    .action("RESEND_EMAIL_VERIFICATION")
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .details(Map.of("email", request.getEmail(), "reason", "resent email verification"))
                    .success(true)
                    .createdAt(TimeUtil.nowUtc())
                    .build());
            return ApiResponse.success("Email xác thực đã được gửi lại vui lòng kiểm tra email");

        }catch (Exception e){
            log.error("Lỗi khi gửi lại đến email {}", request.getEmail(), e);
            return ApiResponse.error("Không thể gửi email xác thực" + e.getMessage());
        }
    }

    public ApiResponse<String> logout(String refreshToken, String ipAddress, String userAgent) {
        try{
            Optional<UserSession> userSession = userSessionRepository.findByRefreshToken(refreshToken);
            if (userSession.isPresent()){
                UserSession session = userSession.get();
                session.invalidate();
                userSessionRepository.save(session);
                User user = session.getUser();
                user.goOffline();
                userRepository.save(user);
            }
            SecurityContextHolder.clearContext();
            return ApiResponse.success("Đăng xuất thành công");
        }catch (Exception e){
            log.error("Lỗi đăng xuất", e);
            return ApiResponse.error("Đăng xuất lỗi");
        }
    }


    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .balance(user.getBalance())
                .isVerified(user.getIsVerified())
                .isEmailVerified(user.getIsEmailVerified())
                .isOnline(user.getIsOnline())
                .lastSeen(user.getLastSeen())
                .socialLinks(user.getSocialLinks())
                .status(user.getStatus().name())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
