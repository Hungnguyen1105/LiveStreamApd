package com.example.livestream_apd.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_email_verified")
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "is_online")
    @Builder.Default
    private Boolean isOnline = false;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_social_links", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "platform")
    @Column(name = "url")
    @Builder.Default
    private Map<String, String> socialLinks = new HashMap<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "followers_count")
    @Builder.Default
    private Integer followersCount = 0;

    @Column(name = "following_count")
    @Builder.Default
    private Integer followingCount = 0;

    @Column(name = "is_private")
    @Builder.Default
    private Boolean isPrivate = false;

    @Column(name = "allow_direct_messages")
    @Builder.Default
    private Boolean allowDirectMessages = false;

    @Column(name = "show_online_status")
    @Builder.Default
    private Boolean showOnlineStatus = false;

    @Column(name = "email_notifications")
    @Builder.Default
    private Boolean emailNotifications = false;

    @Column(name = "push_notifications")
    @Builder.Default
    private Boolean pushNotifications = false;

    @Column(name = "live_notifications")
    @Builder.Default
    private Boolean liveNotifications = false;

    @Column(name = "follow_notifications")
    @Builder.Default
    private Boolean followNotifications = false;

    @Column(name = "message_notifications")
    @Builder.Default
    private Boolean messageNotifications = false;

    //Follow relationship
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_follows",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )

    @Builder.Default()
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> followers = new HashSet<>();

    //block relationship
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_blocks",
            joinColumns = @JoinColumn(name = "blocker_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_id")
    )
    @Builder.Default
    private Set<User> blockedUsers = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        BANNED,
        PENDING_VERIFICATION
    }

    // Helper methods
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean canLogin() {
        return (status == UserStatus.ACTIVE || status == UserStatus.PENDING_VERIFICATION)
                && isEmailVerified;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.isEmailVerified = true;
    }

    public void ban() {
        this.status = UserStatus.BANNED;
        this.isOnline = false;
    }

    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
        this.isOnline = true;
    }
    public void goOffline() {
        this.isOnline = false;
        this.lastSeen = LocalDateTime.now();
    }

    // Role and permission helper methods
    public boolean hasRole(Role.RoleName roleName) {
        return roles != null &&
                roles.stream().anyMatch(role -> role.getName() == roleName);
    }

    public boolean hasPermission(String permissionName) {
        return roles != null &&
                roles.stream().anyMatch(role -> role.hasPermission(permissionName));
    }

    public boolean isAdmin() {
        return hasRole(Role.RoleName.ADMIN);
    }

    public boolean isModerator() {
        return hasRole(Role.RoleName.MODERATOR) || isAdmin();
    }

    public boolean isVip() {
        return hasRole(Role.RoleName.VIP);
    }

    public boolean isStreamer() {
        return hasRole(Role.RoleName.STREAMER);
    }

    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }
}