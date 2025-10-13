package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.application.dto.request.LiveStreamCallbackRequest;
import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.LiveStreamSessionResponse;
import com.example.livestream_apd.application.dto.response.PagedResponse;
import com.example.livestream_apd.domain.entity.LiveStreamSession;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.LiveStreamSessionRepository;
import com.example.livestream_apd.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveStreamService {
    
    private final LiveStreamSessionRepository liveStreamSessionRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    
    @Transactional
    public ApiResponse<String> handleLiveStreamCallback(LiveStreamCallbackRequest request) {
        try {
            switch (request.getAction().toLowerCase()) {
                case "start":
                    return handleLiveStart(request);
                case "end":
                    return handleLiveEnd(request);
                case "join":
                    return handleViewerJoin(request);
                case "leave":
                    return handleViewerLeave(request);
                case "gift":
                    return handleGiftReceived(request);
                case "analytics":
                    return handleAnalytics(request);
                default:
                    log.warn("Unknown callback action: {}", request.getAction());
                    return ApiResponse.error("Unknown action: " + request.getAction());
            }
        } catch (Exception e) {
            log.error("Error handling livestream callback: ", e);
            return ApiResponse.error("Failed to process callback: " + e.getMessage());
        }
    }
    
    @Transactional
    public ApiResponse<String> handleLiveStart(LiveStreamCallbackRequest request) {
        // Check if session already exists
        Optional<LiveStreamSession> existingSession = liveStreamSessionRepository.findByRoomId(request.getRoomId());
        if (existingSession.isPresent()) {
            log.warn("Live session already exists for room: {}", request.getRoomId());
            return ApiResponse.success("Live session already active", "session_exists");
        }
        
        // Create new live session
        LiveStreamSession session = LiveStreamSession.builder()
                .roomId(request.getRoomId())
                .hostId(Long.parseLong(request.getUserId()))
                .title(request.getTitle())
                .startTime(LocalDateTime.now())
                .status(LiveStreamSession.LiveStreamStatus.ACTIVE)
                .build();
        
        liveStreamSessionRepository.save(session);
        log.info("Live stream started: roomId={}, hostId={}", request.getRoomId(), request.getUserId());
        
        return ApiResponse.success("Live stream started successfully", session.getRoomId());
    }
    
    @Transactional
    public ApiResponse<String> handleLiveEnd(LiveStreamCallbackRequest request) {
        Optional<LiveStreamSession> sessionOpt = liveStreamSessionRepository.findByRoomId(request.getRoomId());
        if (sessionOpt.isEmpty()) {
            log.warn("Live session not found for room: {}", request.getRoomId());
            return ApiResponse.error("Live session not found");
        }
        
        LiveStreamSession session = sessionOpt.get();
        session.setEndTime(LocalDateTime.now());
        session.setStatus(LiveStreamSession.LiveStreamStatus.ENDED);
        
        if (request.getDuration() != null) {
            session.setDuration(request.getDuration());
        }
        if (request.getMaxViewers() != null) {
            session.setMaxViewers(request.getMaxViewers());
        }
        if (request.getTotalViews() != null) {
            session.setTotalViews(request.getTotalViews());
        }
        
        liveStreamSessionRepository.save(session);
        log.info("Live stream ended: roomId={}, duration={}s", request.getRoomId(), session.getDuration());
        
        return ApiResponse.success("Live stream ended successfully", session.getRoomId());
    }
    
    @Transactional
    public ApiResponse<String> handleViewerJoin(LiveStreamCallbackRequest request) {
        Optional<LiveStreamSession> sessionOpt = liveStreamSessionRepository.findByRoomId(request.getRoomId());
        if (sessionOpt.isEmpty()) {
            return ApiResponse.error("Live session not found");
        }
        
        LiveStreamSession session = sessionOpt.get();
        if (request.getViewerCount() != null) {
            session.setCurrentViewers(request.getViewerCount());
            // Update max viewers if current is higher
            if (session.getCurrentViewers() > session.getMaxViewers()) {
                session.setMaxViewers(session.getCurrentViewers());
            }
        }
        
        // Increment total views
        session.setTotalViews(session.getTotalViews() + 1);
        
        liveStreamSessionRepository.save(session);
        log.debug("Viewer joined: roomId={}, viewerId={}, currentViewers={}", 
                request.getRoomId(), request.getUserId(), session.getCurrentViewers());
        
        return ApiResponse.success("Viewer join recorded", "joined");
    }
    
    @Transactional
    public ApiResponse<String> handleViewerLeave(LiveStreamCallbackRequest request) {
        Optional<LiveStreamSession> sessionOpt = liveStreamSessionRepository.findByRoomId(request.getRoomId());
        if (sessionOpt.isEmpty()) {
            return ApiResponse.error("Live session not found");
        }
        
        LiveStreamSession session = sessionOpt.get();
        if (request.getViewerCount() != null) {
            session.setCurrentViewers(request.getViewerCount());
        }
        
        liveStreamSessionRepository.save(session);
        log.debug("Viewer left: roomId={}, viewerId={}, currentViewers={}", 
                request.getRoomId(), request.getUserId(), session.getCurrentViewers());
        
        return ApiResponse.success("Viewer leave recorded", "left");
    }
    
    @Transactional
    public ApiResponse<String> handleGiftReceived(LiveStreamCallbackRequest request) {
        // Update live session gift statistics
        Optional<LiveStreamSession> sessionOpt = liveStreamSessionRepository.findByRoomId(request.getRoomId());
        if (sessionOpt.isPresent()) {
            LiveStreamSession session = sessionOpt.get();
            Double giftValue = request.getGiftValue() != null ? request.getGiftValue().doubleValue() : 0.0;
            Integer quantity = request.getGiftQuantity() != null ? request.getGiftQuantity() : 1;
            Double totalGiftValue = giftValue * quantity;
            
            session.setTotalGiftsValue(session.getTotalGiftsValue() + totalGiftValue);
            liveStreamSessionRepository.save(session);
        }
        
        // Process payment for gift (deduct from sender, add to receiver)
        try {
            Long senderId = Long.parseLong(request.getUserId());
            Long receiverId = Long.parseLong(request.getTargetUserId());
            Double giftValue = request.getGiftValue() != null ? request.getGiftValue().doubleValue() : 0.0;
            Integer quantity = request.getGiftQuantity() != null ? request.getGiftQuantity() : 1;
            Double totalAmount = giftValue * quantity;
            
            // Deduct from sender
            paymentService.processGiftPayment(senderId, receiverId, totalAmount, 
                    "Gift: " + request.getGiftId() + " x" + quantity);
            
            log.info("Gift processed: from={}, to={}, gift={}, value={}, quantity={}", 
                    senderId, receiverId, request.getGiftId(), giftValue, quantity);
                    
        } catch (Exception e) {
            log.error("Failed to process gift payment: ", e);
            return ApiResponse.error("Failed to process gift payment: " + e.getMessage());
        }
        
        return ApiResponse.success("Gift received and processed", "gift_processed");
    }
    
    @Transactional
    public ApiResponse<String> handleAnalytics(LiveStreamCallbackRequest request) {
        // Store analytics data (you can extend this to save to a separate analytics table)
        log.info("Analytics received for room {}: {}", request.getRoomId(), request.getStatistics());
        return ApiResponse.success("Analytics recorded", "analytics_saved");
    }
    
    public PagedResponse<LiveStreamSessionResponse> getActiveLiveStreams(Pageable pageable) {
        Page<LiveStreamSession> sessions = liveStreamSessionRepository
                .findActiveLiveStreams(LiveStreamSession.LiveStreamStatus.ACTIVE, pageable);
        
        List<LiveStreamSessionResponse> responses = sessions.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<LiveStreamSessionResponse>builder()
                .content(responses)
                .totalElements(sessions.getTotalElements())
                .totalPages(sessions.getTotalPages())
                .size(sessions.getSize())
                .page(sessions.getNumber())
                .first(sessions.isFirst())
                .last(sessions.isLast())
                .build();
    }
    
    public PagedResponse<LiveStreamSessionResponse> getUserLiveStreams(Long userId, Pageable pageable) {
        Page<LiveStreamSession> sessions = liveStreamSessionRepository.findByHostId(userId, pageable);
        
        List<LiveStreamSessionResponse> responses = sessions.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<LiveStreamSessionResponse>builder()
                .content(responses)
                .totalElements(sessions.getTotalElements())
                .totalPages(sessions.getTotalPages())
                .size(sessions.getSize())
                .page(sessions.getNumber())
                .first(sessions.isFirst())
                .last(sessions.isLast())
                .build();
    }
    
    private LiveStreamSessionResponse convertToResponse(LiveStreamSession session) {
        // Get host username
        String hostUsername = userRepository.findById(session.getHostId())
                .map(User::getUsername)
                .orElse("Unknown");
        
        return LiveStreamSessionResponse.builder()
                .sessionId(session.getId().toString())
                .roomId(session.getRoomId())
                .hostId(session.getHostId().toString())
                .hostUsername(hostUsername)
                .title(session.getTitle())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .duration(session.getDuration())
                .maxViewers(session.getMaxViewers())
                .totalViews(session.getTotalViews())
                .totalGiftsValue(session.getTotalGiftsValue())
                .status(session.getStatus().name().toLowerCase())
                .build();
    }

    // Controller wrapper methods
    public LiveStreamSession getSessionByRoomId(String roomId) {
        return liveStreamSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Live stream session not found for room: " + roomId));
    }

    public LiveStreamSession handleRoomCreated(LiveStreamCallbackRequest request) {
        request.setAction("start");
        handleLiveStreamCallback(request);
        return getSessionByRoomId(request.getRoomId());
    }

    public LiveStreamSession handleRoomDestroyed(LiveStreamCallbackRequest request) {
        request.setAction("end");
        handleLiveStreamCallback(request);
        return getSessionByRoomId(request.getRoomId());
    }

    public LiveStreamSession handleUserJoined(LiveStreamCallbackRequest request) {
        request.setAction("join");
        handleLiveStreamCallback(request);
        return getSessionByRoomId(request.getRoomId());
    }

    public LiveStreamSession handleUserLeft(LiveStreamCallbackRequest request) {
        request.setAction("leave");
        handleLiveStreamCallback(request);
        return getSessionByRoomId(request.getRoomId());
    }

    public void handleGiftSent(LiveStreamCallbackRequest request) {
        request.setAction("gift");
        handleLiveStreamCallback(request);
    }

    public Page<LiveStreamSession> getUserSessions(Long userId, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return liveStreamSessionRepository.findByHostIdOrderByStartTimeDesc(userId, pageable);
    }
}