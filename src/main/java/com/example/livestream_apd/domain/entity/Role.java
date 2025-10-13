package com.example.livestream_apd.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    @Column(length = 200)
    private String description;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users;
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum RoleName {
        USER("Người dùng thông thường"),
        ADMIN("Quản trị viên"),
        MODERATOR("Kiểm duyệt viên"),
        VIP("Người dùng VIP"),
        STREAMER("Người phát sóng");

        private final String displayName;

        RoleName(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public boolean hasPermission(String permissionName) {
        return permissions != null &&
                permissions.stream()
                        .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    public boolean isAdmin() {
        return name == RoleName.ADMIN;
    }

    public boolean isModerator() {
        return name == RoleName.MODERATOR;
    }
    public boolean isVip() {
        return name == RoleName.VIP;
    }

    // Custom equals and hashCode to avoid circular reference issues
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && name == role.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name=" + name +
                ", description='" + description + '\'' +
                '}';
    }
}
