package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.utils.TimeUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
@Service

public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.email.verification-url}")
    private String verificationUrl;

    @Value("${app.email.reset-password-url}")
    private String resetPasswordUrl;

    public CompletableFuture<Boolean> sendEmailVerificationOtp(String toEmail, String fullName,
                                                        String otpCode, String token) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
                messageHelper.setFrom(fromEmail, fromName);
                messageHelper.setTo(toEmail);
                messageHelper.setSubject("Email OTP");
                String htmlContent = createEmailVerificationTemplate(fullName, otpCode, token);
                messageHelper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Gửi thành công đến {}", toEmail);
                return true;
            } catch (Exception e) {
                log.error("Lỗi gửi email {}", toEmail, e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> sendWelcomeEmail(String toEmail, String fullName){
        return CompletableFuture.supplyAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
                messageHelper.setFrom(fromEmail, fromName);
                messageHelper.setTo(toEmail);
                messageHelper.setSubject("Chào mừng đến với Livestream");
                String htmlContent = createWelcomeTemplate(fullName);
                messageHelper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Gửi thành công đến {}", toEmail);
                return true;
            } catch (Exception e) {
                log.error("Lỗi gửi email {}", toEmail, e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> sendResetPasswordEmail(String toEmail, String fullName, String otpCode, String token) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
                messageHelper.setFrom(fromEmail, fromName);
                messageHelper.setTo(toEmail);
                messageHelper.setSubject("Reset Password");
                String htmlContent = createPasswordResetTemplate(fullName, otpCode, token);
                messageHelper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Gửi thành công đến {}", toEmail);
                return true;
            } catch (Exception e){
                log.error("Lỗi gửi email {}", toEmail, e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> sendPasswordChangeConfirmation(String toEmail, String fullName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
                messageHelper.setFrom(fromEmail, fromName);
                messageHelper.setTo(toEmail);
                messageHelper.setSubject("Mật khẩu đã được thay đổi");
                String htmlContent = createWelcomeTemplate(fullName);
                messageHelper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Gửi thành công đến {}", toEmail);
                return true;
            } catch (Exception e) {
                log.error("Lỗi gửi email {}", toEmail, e);
                return false;
            }
        });
    }

    private String createEmailVerificationTemplate(String fullName, String otpCode, String token) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác thực Email</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                    .content { padding: 40px 30px; }
                    .otp-code { background: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                    .otp-number { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Xác thực Email</h1>
                        <p>Live Hungbao - Nền tảng livestream hàng đầu</p>
                    </div>
                    <div class="content">
                        <h2>Chào %s!</h2>
                        <p>Cảm ơn bạn đã đăng ký tài khoản Live Hungbao. Để hoàn tất quá trình đăng ký, vui lòng xác thực email của bạn.</p>
                        
                        <div class="otp-code">
                            <p>Mã xác thực của bạn là:</p>
                            <div class="otp-number">%s</div>
                            <p><small>Mã này có hiệu lực trong 5 phút</small></p>
                        </div>
                        
                        <p>Hoặc bạn có thể click vào nút bên dưới để xác thực:</p>
                        <div style="text-align: center;">
                            <a href="%s?token=%s&otp=%s" class="button">Xác thực Email</a>
                        </div>
                        
                        <p><strong>Lưu ý:</strong></p>
                        <ul>
                            <li>Mã OTP này chỉ có hiệu lực trong 5 phút</li>
                            <li>Không chia sẻ mã này với bất kỳ ai</li>
                            <li>Nếu bạn không thực hiện hành động này, vui lòng bỏ qua email</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>© 2024 Live Hungbao. Tất cả quyền được bảo lưu.</p>
                        <p>Email được gửi lúc: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                fullName, otpCode, verificationUrl, token, otpCode,
                TimeUtil.nowVietNam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
    }

    private String createPasswordResetTemplate(String fullName, String otpCode, String token) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đặt lại mật khẩu</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #ff6b6b 0%%, #ffa500 100%%); color: white; padding: 30px; text-align: center; }
                    .content { padding: 40px 30px; }
                    .otp-code { background: #fff5f5; border: 2px dashed #ff6b6b; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                    .otp-number { font-size: 32px; font-weight: bold; color: #ff6b6b; letter-spacing: 5px; }
                    .button { display: inline-block; background: #ff6b6b; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 6px; padding: 15px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔄 Đặt lại mật khẩu</h1>
                        <p>Live Hungbao - Khôi phục tài khoản</p>
                    </div>
                    <div class="content">
                        <h2>Chào %s!</h2>
                        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                        
                        <div class="otp-code">
                            <p>Mã xác thực đặt lại mật khẩu:</p>
                            <div class="otp-number">%s</div>
                            <p><small>Mã này có hiệu lực trong 5 phút</small></p>
                        </div>
                        
                        <p>Hoặc bạn có thể click vào nút bên dưới:</p>
                        <div style="text-align: center;">
                            <a href="%s?token=%s&otp=%s" class="button">Đặt lại mật khẩu</a>
                        </div>
                        
                        <div class="warning">
                            <p><strong>⚠️ Lưu ý bảo mật:</strong></p>
                            <ul>
                                <li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>
                                <li>Không chia sẻ mã OTP với bất kỳ ai</li>
                                <li>Mã này sẽ hết hạn sau 5 phút</li>
                                <li>Liên hệ hỗ trợ nếu có vấn đề bất thường</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2024 Live Hungbao. Tất cả quyền được bảo lưu.</p>
                        <p>Email được gửi lúc: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                fullName, otpCode, resetPasswordUrl, token, otpCode,
                TimeUtil.nowVietNam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
    }

    private String createWelcomeTemplate(String fullName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Chào mừng đến với Live Hungbao</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #00c851 0%%, #007e33 100%%); color: white; padding: 30px; text-align: center; }
                    .content { padding: 40px 30px; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
                    .feature { background: #f8f9fa; border-radius: 8px; padding: 20px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Chào mừng đến với Live Hungbao!</h1>
                        <p>Nền tảng livestream tương tác số 1 Việt Nam</p>
                    </div>
                    <div class="content">
                        <h2>Chào %s!</h2>
                        <p>Cảm ơn bạn đã tham gia cộng đồng Live Hungbao! Tài khoản của bạn đã được kích hoạt thành công.</p>
                        
                        <div class="feature">
                            <h3>🎥 Bắt đầu livestream ngay</h3>
                            <p>Chia sẻ những khoảnh khắc tuyệt vời với hàng triệu người xem</p>
                        </div>
                        
                        <div class="feature">
                            <h3>🎁 Nhận và tặng quà ảo</h3>
                            <p>Tương tác với streamer yêu thích thông qua hệ thống quà tặng đa dạng</p>
                        </div>
                        
                        <div class="feature">
                            <h3>💬 Trò chuyện realtime</h3>
                            <p>Kết nối với cộng đồng thông qua chat trực tiếp</p>
                        </div>
                        
                        <p>Hãy khám phá và trải nghiệm những tính năng tuyệt vời của Live Hungbao!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Live Hungbao. Tất cả quyền được bảo lưu.</p>
                        <p>Cần hỗ trợ? Liên hệ: support@livehungbao.com</p>
                    </div>
                </div>
            </body>
            </html>            """, fullName);
    }

    private String createPasswordChangeConfirmationTemplate(String fullName) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset=\"UTF-8\">" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                        "<title>Mật khẩu đã được thay đổi</title>" +
                        "<style>" +
                        "body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                        ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }" +
                        ".header { background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; padding: 30px; text-align: center; }" +
                        ".content { padding: 40px 30px; }" +
                        ".security-notice { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 6px; padding: 15px; margin: 20px 0; }" +
                        ".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class=\"container\">" +
                        "<div class=\"header\">" +
                        "<h1>🔐 Mật khẩu đã được thay đổi</h1>" +
                        "<p>Live Hungbao - Bảo mật tài khoản</p>" +
                        "</div>" +
                        "<div class=\"content\">" +
                        "<h2>Chào %s!</h2>" +
                        "<p>Mật khẩu tài khoản Live Hungbao của bạn đã được thay đổi thành công.</p>" +

                        "<div class=\"security-notice\">" +
                        "<h3>⚠️ Thông báo bảo mật</h3>" +
                        "<p>Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ ngay với đội hỗ trợ của chúng tôi.</p>" +
                        "</div>" +

                        "<p><strong>Thông tin thay đổi:</strong></p>" +
                        "<ul>" +
                        "<li>Thời gian: %s</li>" +
                        "<li>Tất cả phiên đăng nhập khác đã bị đăng xuất tự động</li>" +
                        "<li>Bạn cần đăng nhập lại để tiếp tục sử dụng</li>" +
                        "</ul>" +

                        "<p><strong>Để bảo vệ tài khoản:</strong></p>" +
                        "<ul>" +
                        "<li>Không chia sẻ mật khẩu với bất kỳ ai</li>" +
                        "<li>Sử dụng mật khẩu mạnh và duy nhất</li>" +
                        "<li>Kích hoạt xác thực 2 yếu tố nếu có thể</li>" +
                        "</ul>" +
                        "</div>" +
                        "<div class=\"footer\">" +
                        "<p>© 2024 Live Hungbao. Tất cả quyền được bảo lưu.</p>" +
                        "<p>Cần hỗ trợ? Liên hệ: support@livehungbao.com</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                fullName, TimeUtil.nowVietNam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
    }

    public void sendWithdrawOTP(String email, String fullName, String otpCode, String amount) {
        String subject = "Mã OTP xác thực rút tiền - Live Hungbao";
        String body = String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset=\"UTF-8\">" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                        ".header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; }" +
                        ".content { background: #f9f9f9; padding: 30px; }" +
                        ".otp-box { background: white; border: 2px solid #667eea; padding: 20px; text-align: center; margin: 20px 0; }" +
                        ".otp-code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; }" +
                        ".warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; margin: 20px 0; }" +
                        ".footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class=\"container\">" +
                        "<div class=\"header\">" +
                        "<h1>🔐 Xác thực rút tiền</h1>" +
                        "</div>" +
                        "<div class=\"content\">" +
                        "<h2>Xin chào %s,</h2>" +
                        "<p>Bạn đã yêu cầu rút tiền với số tiền <strong>%s VND</strong>.</p>" +
                        "<p>Vui lòng sử dụng mã OTP dưới đây để xác thực giao dịch:</p>" +
                        "<div class=\"otp-box\">" +
                        "<div class=\"otp-code\">%s</div>" +
                        "<p><small>Mã có hiệu lực trong 5 phút</small></p>" +
                        "</div>" +
                        "<div class=\"warning\">" +
                        "<strong>⚠️ Lưu ý quan trọng:</strong>" +
                        "<ul>" +
                        "<li>Không chia sẻ mã OTP với bất kỳ ai</li>" +
                        "<li>Mã OTP chỉ có hiệu lực trong 5 phút</li>" +
                        "<li>Nếu bạn không thực hiện giao dịch này, vui lòng liên hệ hỗ trợ ngay</li>" +
                        "</ul>" +
                        "</div>" +
                        "<p>Trân trọng,<br>Đội ngũ Live Hungbao</p>" +
                        "<p><em>Thời gian: %s</em></p>" +
                        "</div>" +
                        "<div class=\"footer\">" +
                        "<p>© 2024 Live Hungbao. Tất cả quyền được bảo lưu.</p>" +
                        "<p>Cần hỗ trợ? Liên hệ: support@livehungbao.com</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                fullName, amount, otpCode, TimeUtil.nowVietNam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
            messageHelper.setFrom(fromEmail, fromName);
            messageHelper.setTo(email);
            messageHelper.setSubject(subject);
            messageHelper.setText(body, true);
            mailSender.send(message);
            log.info("OTP withdrawal email sent successfully to {}", email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error sending OTP withdrawal email to {}", email, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
