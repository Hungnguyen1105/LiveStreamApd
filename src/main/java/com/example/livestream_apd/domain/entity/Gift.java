package com.example.livestream_apd.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "gifts", indexes = {
        @Index(name = "idx_gifts_is_active", columnList = "is_active"),
        @Index(name = "idx_gifts_category", columnList = "category"),
        @Index(name = "idx_gifts_price", columnList = "price"),
        @Index(name = "idx_gifts_popularity", columnList = "popularity_score")
})
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "icon_url", columnDefinition = "TEXT", nullable = false)
    private String iconUrl;

    @Column(name = "animation_url", columnDefinition = "TEXT")
    private String animationUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "gift_animation_config", joinColumns = @JoinColumn(name = "gift_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value", columnDefinition = "TEXT")
    private Map<String, String> animationConfig;

    @Column(length = 50)
    private String category;

    @Column(name = "popularity_score")
    @Builder.Default
    private Integer popularityScore = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "animation_duration")
    private Integer animationDuration; // in milliseconds

    @Column(name = "sound_url", columnDefinition = "TEXT")
    private String soundUrl;

    @Column(name = "is_premium")
    @Builder.Default
    private Boolean isPremium = false;

    @Column(name = "min_level")
    @Builder.Default
    private Integer minLevel = 1;

    @Column(name = "max_quantity_per_send")
    @Builder.Default
    private Integer maxQuantityPerSend = 100;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void incrementPopularity() {
        this.popularityScore = (this.popularityScore == null ? 0 : this.popularityScore) + 1;
    }

    public void incrementPopularity(int amount) {
        this.popularityScore = (this.popularityScore == null ? 0 : this.popularityScore) + amount;
    }

    public boolean isAvailable() {
        return this.isActive != null && this.isActive;
    }

    public BigDecimal calculateTotalPrice(int quantity) {
        return this.price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gift)) return false;
        Gift gift = (Gift) o;
        return id != null && id.equals(gift.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
