package com.example.livestream_apd.application.dto.request;

import com.example.livestream_apd.domain.entity.ChatRoom;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {

    @NotNull(message = "Livestream ID is required")
    private Long livestreamId;

    @NotNull(message = "Room type is required")
    private ChatRoom.RoomType roomType;

    @Size(max = 1000, message = "Settings cannot exceed 1000 characters")
    @Builder.Default
    private Map<String, String> settings = new HashMap<>();
}
