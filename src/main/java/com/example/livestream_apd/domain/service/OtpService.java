package com.example.livestream_apd.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.livestream_apd.utils.TimeUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
public class OtpService {
    @Value("6")
    private int otpLength;
    private final Random random = new SecureRandom();

    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for(int i = 0; i < otpLength; i++){
            otp.append(random.nextInt(10));
        }
        String otpString = otp.toString();
        log.info("Generated OTP: {}", otpString);
        return otpString;
    }

    public Boolean validateOtp(String providedOtp, String storedOtp) {
        if(providedOtp == null || storedOtp == null){
            return false;
        }
        boolean otpMatch = storedOtp.trim().equals(providedOtp.trim());
        log.info("OTP Match: {}", otpMatch);
        return otpMatch;
    }

    public Boolean validateOtpWithExpiry(String providedOtp, String storedOtp, LocalDateTime expiryTime) {
        if(providedOtp == null || storedOtp == null || expiryTime == null){
            return false;
        }
        
        if (TimeUtil.isBeforeNowUtc(expiryTime)) {
            log.info("OTP expired at: {}", expiryTime);
            return false;
        }
        
        boolean otpMatch = storedOtp.trim().equals(providedOtp.trim());
        log.info("OTP Match: {}", otpMatch);
        return otpMatch;
    }

    public LocalDateTime getOTPExpiryTime() {
        return TimeUtil.nowUtcPlusMinutes(5); // 5 minutes expiry
    }


}
