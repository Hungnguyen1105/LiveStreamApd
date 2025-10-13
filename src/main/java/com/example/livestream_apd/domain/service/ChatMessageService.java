package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.application.dto.request.EditMessageRequest;
import com.example.livestream_apd.application.dto.request.ReactToMessageRequest;
import com.example.livestream_apd.application.dto.request.SendMessageRequest;
import com.example.livestream_apd.application.dto.request.TypingIndicatorRequest;
import com.example.livestream_apd.application.dto.response.ChatMessageResponse;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatMessageService {
    
    ChatMessageResponse sendMessage(SendMessageRequest request, User currentUser);
    
    Page<ChatMessageResponse> getMessagesInRoom(Long chatRoomId, Pageable pageable);
    
    ChatMessageResponse editMessage(Long messageId, EditMessageRequest request, User currentUser);
    
    void deleteMessage(Long messageId, User currentUser);
    
    ChatMessageResponse reactToMessage(Long messageId, ReactToMessageRequest request, User currentUser);
    
    void removeReaction(Long messageId, String reactionType, User currentUser);
    
    Page<ChatMessageResponse> getDirectMessages(Long userId, User currentUser, Pageable pageable);
    
    void sendTypingIndicator(TypingIndicatorRequest request, User currentUser);
    
    Page<ChatMessageResponse> searchMessages(Long chatRoomId, String keyword, Pageable pageable);
    
    ChatMessageResponse getMessageById(Long messageId);
}
