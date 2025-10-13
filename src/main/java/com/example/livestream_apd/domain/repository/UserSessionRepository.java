package com.example.livestream_apd.domain.repository;


import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionToken(String sessionToken);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findByUserAndIsActiveTrue(User user);

    List<UserSession> findByUserAndIsActiveTrueOrderByLastActivityDesc(User user);

    @Query("SELECT s FROM UserSession s WHERE s.user = :user AND s.isActive = true " +
            "AND s.expiresAt > :now ORDER BY s.lastActivity DESC")
    List<UserSession> findValidSessionsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user = :user")
    void invalidateAllUserSessions(@Param("user") User user);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user = :user AND s.id != :currentSessionId")
    void invalidateOtherUserSessions(@Param("user") User user, @Param("currentSessionId") Long currentSessionId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expiresAt < :now")
    void invalidateExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.isActive = false AND s.createdAt < :cutoffTime")
    void deleteOldInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user = :user AND s.isActive = true")
    long countActiveSessionsByUser(@Param("user") User user);

    @Query("SELECT s FROM UserSession s WHERE s.lastActivity < :cutoffTime AND s.isActive = true")
    List<UserSession> findStaleActiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
}
