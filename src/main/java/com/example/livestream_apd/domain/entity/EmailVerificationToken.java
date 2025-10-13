package com.example.livestream_apd.domain.entity;

import com.example.livestream_apd.utils.TimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(nullable = false)
    private String email;

    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "max_attempts")
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "is_used")
    @Builder.Default
    private Boolean isUsed = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = TimeUtil.nowUtc();
        }

    }

    // Helper methods
    public boolean isExpired() {
        return TimeUtil.nowUtc().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isUsed && !isExpired() && attempts < maxAttempts;
    }

    public boolean canAttempt() {
        return !isUsed && !isExpired() && attempts < maxAttempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void markAsUsed() {
        this.isUsed = true;
    }

    public boolean hasExceededMaxAttempts() {
        return attempts >= maxAttempts;
    }
}