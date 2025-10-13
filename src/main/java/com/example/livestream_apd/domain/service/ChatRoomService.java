package com.example.livestream_apd.domain.service;

import com.example.livestream_apd.application.dto.request.CreateChatRoomRequest;
import com.example.livestream_apd.application.dto.response.ChatRoomResponse;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRoomService {
    
    ChatRoomResponse createChatRoom(CreateChatRoomRequest request, User currentUser);
    
    Page<ChatRoomResponse> getChatRooms(Pageable pageable);
    
    ChatRoomResponse getChatRoomById(Long id);
    
    ChatRoomResponse getChatRoomByLivestream(Long livestreamId);
    
    void deleteChatRoom(Long id, User currentUser);
    
    void deactivateChatRoom(Long id, User currentUser);
    
    void activateChatRoom(Long id, User currentUser);
}
