package com.example.livestream_apd.application.dto.response;

import com.example.livestream_apd.domain.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    
    private Long id;
    private Long livestreamId;
    private ChatRoom.RoomType roomType;
    private Map<String, String> settings;
    private Boolean isActive;
    private Integer messageCount;
    private ChatMessageResponse lastMessage;
    private LocalDateTime createdAt;
}
