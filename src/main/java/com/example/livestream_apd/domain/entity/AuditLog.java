package com.example.livestream_apd.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "audit_log_details", joinColumns = @JoinColumn(name = "audit_log_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value", columnDefinition = "TEXT")
    private Map<String, String> details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Builder.Default
    private Boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Static factory methods for common audit actions
    public static AuditLog loginSuccess(User user, String ipAddress, String userAgent) {
        return AuditLog.builder()
                .user(user)
                .action("LOGIN_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }

    public static AuditLog loginFailure(String email, String ipAddress, String userAgent, String errorMessage) {
        return AuditLog.builder()
                .action("LOGIN_FAILURE")
                .details(Map.of("email", email))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    public static AuditLog registrationSuccess(User user, String ipAddress, String userAgent) {
        return AuditLog.builder()
                .user(user)
                .action("REGISTRATION_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }

    public static AuditLog emailVerificationSuccess(User user, String ipAddress, String userAgent) {
        return AuditLog.builder()
                .user(user)
                .action("EMAIL_VERIFICATION_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }

    public static AuditLog passwordResetRequest(User user, String ipAddress, String userAgent) {
        return AuditLog.builder()
                .user(user)
                .action("PASSWORD_RESET_REQUEST")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }

    public static AuditLog passwordResetSuccess(User user, String ipAddress, String userAgent) {
        return AuditLog.builder()
                .user(user)
                .action("PASSWORD_RESET_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }
}
