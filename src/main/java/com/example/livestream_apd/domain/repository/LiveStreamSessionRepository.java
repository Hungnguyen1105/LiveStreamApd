package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.LiveStreamSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveStreamSessionRepository extends JpaRepository<LiveStreamSession, Long> {
    
    Optional<LiveStreamSession> findByRoomId(String roomId);
    
    List<LiveStreamSession> findByHostIdAndStatus(Long hostId, LiveStreamSession.LiveStreamStatus status);
    
    Page<LiveStreamSession> findByHostId(Long hostId, Pageable pageable);
    
    Page<LiveStreamSession> findByHostIdOrderByStartTimeDesc(Long hostId, Pageable pageable);
    
    Page<LiveStreamSession> findByStatus(LiveStreamSession.LiveStreamStatus status, Pageable pageable);
    
    @Query("SELECT l FROM LiveStreamSession l WHERE l.status = :status ORDER BY l.startTime DESC")
    Page<LiveStreamSession> findActiveLiveStreams(@Param("status") LiveStreamSession.LiveStreamStatus status, Pageable pageable);
    
    @Query("SELECT l FROM LiveStreamSession l WHERE l.hostId = :hostId AND l.startTime >= :fromDate ORDER BY l.startTime DESC")
    List<LiveStreamSession> findByHostIdAndStartTimeAfter(@Param("hostId") Long hostId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(l) FROM LiveStreamSession l WHERE l.status = :status")
    Long countByStatus(@Param("status") LiveStreamSession.LiveStreamStatus status);
    
    @Query("SELECT SUM(l.totalGiftsValue) FROM LiveStreamSession l WHERE l.hostId = :hostId AND l.endTime >= :fromDate")
    Double sumTotalGiftsValueByHostIdAndPeriod(@Param("hostId") Long hostId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT SUM(l.totalViews) FROM LiveStreamSession l WHERE l.hostId = :hostId")
    Long sumTotalViewsByHostId(@Param("hostId") Long hostId);
}