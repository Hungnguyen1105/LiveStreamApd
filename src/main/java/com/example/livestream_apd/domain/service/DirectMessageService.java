package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.application.dto.request.EditDirectMessageRequest;
import com.example.livestream_apd.application.dto.request.SearchDirectMessageRequest;
import com.example.livestream_apd.application.dto.request.SendDirectMessageRequest;
import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.DirectMessageResponse;
import com.example.livestream_apd.application.dto.response.PagedResponse;
import com.example.livestream_apd.domain.entity.DirectMessage;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DirectMessageService {
    
    ApiResponse<DirectMessageResponse> sendMessage(Long currentUserId, SendDirectMessageRequest request);
    
    ApiResponse<PagedResponse<DirectMessageResponse>> getMessages(Long conversationId, Long currentUserId, Pageable pageable);
    
    ApiResponse<DirectMessageResponse> getMessage(Long messageId, Long currentUserId);
    
    ApiResponse<DirectMessageResponse> editMessage(Long currentUserId, EditDirectMessageRequest request);
    
    ApiResponse<String> deleteMessage(Long messageId, Long currentUserId);
    
    ApiResponse<String> markMessageAsRead(Long messageId, Long currentUserId);
    
    ApiResponse<String> markAllMessagesAsRead(Long conversationId, Long currentUserId);
    
    ApiResponse<PagedResponse<DirectMessageResponse>> searchMessages(
            Long conversationId, Long currentUserId, SearchDirectMessageRequest request);
    
    ApiResponse<PagedResponse<DirectMessageResponse>> getMediaMessages(
            Long conversationId, Long currentUserId, 
            List<DirectMessage.MessageType> mediaTypes, Pageable pageable);
    
    ApiResponse<List<DirectMessageResponse>> getNewMessages(
            Long conversationId, Long currentUserId, LocalDateTime after);
    
    ApiResponse<Long> getUnreadCount(Long conversationId, Long currentUserId);
}
