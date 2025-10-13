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
@Table(name = "permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(length = 50)
    private String category;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Common permission names as constants
    public static final String USER_READ = "USER_READ";
    public static final String USER_WRITE = "USER_WRITE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String USER_MANAGE = "USER_MANAGE";

    public static final String ADMIN_READ = "ADMIN_READ";
    public static final String ADMIN_WRITE = "ADMIN_WRITE";
    public static final String ADMIN_DELETE = "ADMIN_DELETE";
    public static final String ADMIN_MANAGE = "ADMIN_MANAGE";

    public static final String CONTENT_CREATE = "CONTENT_CREATE";
    public static final String CONTENT_EDIT = "CONTENT_EDIT";
    public static final String CONTENT_DELETE = "CONTENT_DELETE";
    public static final String CONTENT_MODERATE = "CONTENT_MODERATE";

    public static final String PAYMENT_READ = "PAYMENT_READ";
    public static final String PAYMENT_WRITE = "PAYMENT_WRITE";
    public static final String PAYMENT_MANAGE = "PAYMENT_MANAGE";

    public static final String STREAM_CREATE = "STREAM_CREATE";
    public static final String STREAM_MANAGE = "STREAM_MANAGE";
    public static final String STREAM_MODERATE = "STREAM_MODERATE";
    public static final String REPORT_VIEW = "REPORT_VIEW";
    public static final String REPORT_MANAGE = "REPORT_MANAGE";

    // Custom equals and hashCode to avoid circular reference issues
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission permission = (Permission) o;
        return Objects.equals(id, permission.id) && Objects.equals(name, permission.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
