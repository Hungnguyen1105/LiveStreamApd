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
@Table(name = "user_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_token", unique = true, nullable = false)
    private String sessionToken;

    @Column(name = "refresh_token", unique = true, nullable = false)
    private String refreshToken;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "session_device_info", joinColumns = @JoinColumn(name = "session_id"))
    @MapKeyColumn(name = "property")
    @Column(name = "value")
    private Map<String, String> deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return isActive && !isExpired();
    }

    public void invalidate() {
        this.isActive = false;
    }

    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    public void extendExpiration(LocalDateTime newExpiresAt) {
        this.expiresAt = newExpiresAt;
        updateLastActivity();
    }
}