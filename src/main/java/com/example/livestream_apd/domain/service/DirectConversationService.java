package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.application.dto.request.StartConversationRequest;
import com.example.livestream_apd.application.dto.response.ApiResponse;
import com.example.livestream_apd.application.dto.response.DirectConversationResponse;
import com.example.livestream_apd.application.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface DirectConversationService {
    
    ApiResponse<DirectConversationResponse> startConversation(Long currentUserId, StartConversationRequest request);
    
    ApiResponse<PagedResponse<DirectConversationResponse>> getConversations(Long userId, Pageable pageable);
    
    ApiResponse<DirectConversationResponse> getConversation(Long conversationId, Long currentUserId);
    
    ApiResponse<DirectConversationResponse> getConversationWithUser(Long currentUserId, Long targetUserId);
    
    ApiResponse<String> deleteConversation(Long conversationId, Long currentUserId);
    
    ApiResponse<String> blockConversation(Long conversationId, Long currentUserId);
    
    ApiResponse<String> unblockConversation(Long conversationId, Long currentUserId);
    
    ApiResponse<PagedResponse<DirectConversationResponse>> searchConversations(Long userId, String query, Pageable pageable);
    
    ApiResponse<PagedResponse<DirectConversationResponse>> getConversationsWithUnreadMessages(Long userId, Pageable pageable);
    
    ApiResponse<Long> getUnreadConversationsCount(Long userId);
    
    ApiResponse<String> markConversationAsRead(Long conversationId, Long currentUserId);
}
