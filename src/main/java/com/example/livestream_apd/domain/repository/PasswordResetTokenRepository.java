package com.example.livestream_apd.domain.repository;


import com.example.livestream_apd.domain.entity.PasswordResetToken;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByOtpCodeAndEmail(String otpCode, String email);

    Optional<PasswordResetToken> findByUserAndIsUsedFalseAndExpiresAtAfter(
            User user, LocalDateTime now);

    List<PasswordResetToken> findByUserAndIsUsedFalse(User user);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.user = :user AND t.isUsed = false " +
            "AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    List<PasswordResetToken> findValidTokensByUser(@Param("user") User user,
                                                   @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.isUsed = true WHERE t.user = :user AND t.isUsed = false")
    void invalidateAllUserTokens(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :cutoffTime")
    void deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.email = :email " +
            "AND t.createdAt > :since AND t.isUsed = false")
    long countRecentTokensByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    boolean existsByUserAndIsUsedFalseAndExpiresAtAfter(User user, LocalDateTime now);
}
