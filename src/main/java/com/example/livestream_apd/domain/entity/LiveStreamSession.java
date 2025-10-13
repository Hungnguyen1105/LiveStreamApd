package com.example.livestream_apd.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "livestream_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStreamSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", unique = true, nullable = false)
    private String roomId;
    
    @Column(name = "host_id", nullable = false)
    private Long hostId;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration") // in seconds
    private Long duration;
    
    @Builder.Default
    @Column(name = "max_viewers")
    private Integer maxViewers = 0;
    
    @Builder.Default
    @Column(name = "total_views")
    private Integer totalViews = 0;
    
    @Builder.Default
    @Column(name = "current_viewers")
    private Integer currentViewers = 0;
    
    @Builder.Default
    @Column(name = "total_gifts_value")
    private Double totalGiftsValue = 0.0;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LiveStreamStatus status = LiveStreamStatus.ACTIVE;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum LiveStreamStatus {
        ACTIVE,
        ENDED,
        CANCELLED
    }
}