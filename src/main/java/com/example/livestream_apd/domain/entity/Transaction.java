package com.example.livestream_apd.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transactions_user_id", columnList = "user_id"),
        @Index(name = "idx_transactions_type", columnList = "type"),
        @Index(name = "idx_transactions_status", columnList = "status"),
        @Index(name = "idx_transactions_created_at", columnList = "created_at"),
        @Index(name = "idx_transactions_vnpay_id", columnList = "vnpay_transaction_id")
})
@Getter
@Setter
@ToString(exclude = {"user"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "vnpay_transaction_id")
    private String vnpayTransactionId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "transaction_metadata", joinColumns = @JoinColumn(name = "transaction_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", columnDefinition = "TEXT")
    @JsonIgnore
    private Map<String, String> metadata;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionType {
        TOPUP("Nạp tiền"),
        GIFT_PURCHASE("Mua quà tặng"),
        GIFT_INCOME("Thu nhập từ quà tặng"),
        GIFT_SENT("Tặng quà"),
        GIFT_RECEIVED("Nhận quà"),
        WITHDRAWAL("Rút tiền"),
        COMMISSION("Hoa hồng"),
        REFUND("Hoàn tiền"),
        PENALTY("Phạt"),
        BONUS("Thưởng");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TransactionStatus {
        PENDING("Đang xử lý"),
        COMPLETED("Hoàn thành"),
        FAILED("Thất bại"),
        CANCELLED("Đã hủy"),
        REFUNDED("Đã hoàn tiền");

        private final String displayName;

        TransactionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }

    public boolean isCompleted() {
        return this.status == TransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == TransactionStatus.FAILED;
    }

    public boolean isCancelled() {
        return this.status == TransactionStatus.CANCELLED;
    }

    public boolean isRefunded() {
        return this.status == TransactionStatus.REFUNDED;
    }

    public boolean isCredit() {
        return type == TransactionType.TOPUP || 
               type == TransactionType.GIFT_INCOME || 
               type == TransactionType.COMMISSION ||
               type == TransactionType.REFUND ||
               type == TransactionType.BONUS;
    }

    public boolean isDebit() {
        return type == TransactionType.GIFT_PURCHASE || 
               type == TransactionType.WITHDRAWAL ||
               type == TransactionType.PENALTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
