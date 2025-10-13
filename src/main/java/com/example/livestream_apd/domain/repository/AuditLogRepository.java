package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.AuditLog;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);

    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.action = :action " +
            "ORDER BY a.createdAt DESC")
    List<AuditLog> findByUserAndAction(@Param("user") User user, @Param("action") String action);

    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress " +
            "AND a.createdAt >= :startTime ORDER BY a.createdAt DESC")
    List<AuditLog> findByIpAddressAndTimeRange(@Param("ipAddress") String ipAddress,
                                               @Param("startTime") LocalDateTime startTime);

    @Query("SELECT a FROM AuditLog a WHERE a.success = false " +
            "AND a.createdAt >= :startTime ORDER BY a.createdAt DESC")
    List<AuditLog> findFailedActionsSince(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action " +
            "AND a.createdAt >= :startTime AND a.success = true")
    long countSuccessfulActionsSince(@Param("action") String action,
                                     @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action " +
            "AND a.ipAddress = :ipAddress AND a.createdAt >= :startTime")
    long countActionsByIpSince(@Param("action") String action,
                               @Param("ipAddress") String ipAddress,
                               @Param("startTime") LocalDateTime startTime);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType " +
            "AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    List<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType,
                                               @Param("entityId") Long entityId);
}
