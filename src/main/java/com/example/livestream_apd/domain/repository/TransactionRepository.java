package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.Transaction;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByVnpayTransactionId(String vnpayTransactionId);

    Page<Transaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Transaction> findByUserAndTypeOrderByCreatedAtDesc(User user, Transaction.TransactionType type, Pageable pageable);

    Page<Transaction> findByUserAndStatusOrderByCreatedAtDesc(User user, Transaction.TransactionStatus status, Pageable pageable);

    Page<Transaction> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.status = :status " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserAndTypeAndStatusOrderByCreatedAtDesc(
            @Param("user") User user, 
            @Param("type") Transaction.TransactionType type, 
            @Param("status") Transaction.TransactionStatus status, 
            Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.status = 'COMPLETED' " +
            "AND t.type IN ('TOPUP', 'GIFT_INCOME', 'COMMISSION', 'REFUND', 'BONUS')")
    BigDecimal getTotalCreditAmount(@Param("user") User user);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.status = 'COMPLETED' " +
            "AND t.type IN ('GIFT_PURCHASE', 'WITHDRAWAL', 'PENALTY')")
    BigDecimal getTotalDebitAmount(@Param("user") User user);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type " +
            "AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateRange(
            @Param("user") User user, 
            @Param("type") Transaction.TransactionType type,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.status = 'PENDING'")
    long countPendingTransactionsByUser(@Param("user") User user);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.createdAt < :cutoffTime")
    List<Transaction> findStaleTransactions(@Param("cutoffTime") LocalDateTime cutoffTime);

    boolean existsByUserAndVnpayTransactionId(User user, String vnpayTransactionId);

    // Statistics queries
    @Query("SELECT DATE(t.createdAt) as date, SUM(t.amount) as amount " +
            "FROM Transaction t WHERE t.user = :user AND t.status = 'COMPLETED' " +
            "AND t.type = :type AND t.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt)")
    List<Object[]> getDailyStatistics(
            @Param("user") User user, 
            @Param("type") Transaction.TransactionType type,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MONTH(t.createdAt) as month, YEAR(t.createdAt) as year, SUM(t.amount) as amount " +
            "FROM Transaction t WHERE t.user = :user AND t.status = 'COMPLETED' " +
            "AND t.type = :type AND t.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(t.createdAt), MONTH(t.createdAt) " +
            "ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)")
    List<Object[]> getMonthlyStatistics(
            @Param("user") User user, 
            @Param("type") Transaction.TransactionType type,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
}
