package com.example.livestream_apd.domain.service.impl;

import com.example.livestream_apd.application.dto.request.CreateChatRoomRequest;
import com.example.livestream_apd.application.dto.response.ChatRoomResponse;
import com.example.livestream_apd.domain.entity.ChatRoom;
import com.example.livestream_apd.domain.entity.Role;
import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.ChatRoomRepository;
import com.example.livestream_apd.domain.repository.ChatMessageRepository;
import com.example.livestream_apd.domain.service.ChatRoomService;
import com.example.livestream_apd.utils.exceptions.ResourceForbiddenException;
import com.example.livestream_apd.utils.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional
    public ChatRoomResponse createChatRoom(CreateChatRoomRequest request, User currentUser) {
        // Check if user has permission to create chat room
        if (!currentUser.hasRole(Role.RoleName.MODERATOR) && !currentUser.hasRole(Role.RoleName.ADMIN)) {
            throw new ResourceForbiddenException("Không có quyền tạo phòng chat");
        }

        // Check if chat room already exists for this livestream
        if (chatRoomRepository.existsByLivestreamIdAndRoomType(request.getLivestreamId(), request.getRoomType())) {
            throw new ResourceForbiddenException("Phòng chat đã tồn tại cho livestream này");
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .livestreamId(request.getLivestreamId())
                .roomType(request.getRoomType())
                .settings(request.getSettings())
                .isActive(true)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);
        log.info("Chat room created: {} by user: {}", chatRoom.getId(), currentUser.getId());

        return mapToResponse(chatRoom);
    }

    @Override
    public Page<ChatRoomResponse> getChatRooms(Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomRepository.findAllActiveRooms(pageable);
        return chatRooms.map(this::mapToResponse);
    }

    @Override
    public ChatRoomResponse getChatRoomById(Long id) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", id));
        return mapToResponse(chatRoom);
    }

    @Override
    public ChatRoomResponse getChatRoomByLivestream(Long livestreamId) {
        ChatRoom chatRoom = chatRoomRepository.findByLivestreamIdAndRoomType(
                        livestreamId, ChatRoom.RoomType.LIVESTREAM)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "livestreamId", livestreamId));
        return mapToResponse(chatRoom);
    }

    @Override
    @Transactional
    public void deleteChatRoom(Long id, User currentUser) {
        if (!currentUser.hasRole(Role.RoleName.MODERATOR) && !currentUser.hasRole(Role.RoleName.ADMIN)) {
            throw new ResourceForbiddenException("Không có quyền xóa phòng chat");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", id));

        // Soft delete all messages in the room
        chatMessageRepository.softDeleteAllInRoom(chatRoom);

        // Delete the chat room
        chatRoomRepository.delete(chatRoom);
        log.info("Chat room deleted: {} by user: {}", id, currentUser.getId());
    }

    @Override
    @Transactional
    public void deactivateChatRoom(Long id, User currentUser) {
        if (!currentUser.hasRole(Role.RoleName.MODERATOR) && !currentUser.hasRole(Role.RoleName.ADMIN)) {
            throw new ResourceForbiddenException("Không có quyền vô hiệu hóa phòng chat");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", id));

        chatRoom.deactivate();
        chatRoomRepository.save(chatRoom);
        log.info("Chat room deactivated: {} by user: {}", id, currentUser.getId());
    }

    @Override
    @Transactional
    public void activateChatRoom(Long id, User currentUser) {
        if (!currentUser.hasRole(Role.RoleName.MODERATOR) && !currentUser.hasRole(Role.RoleName.ADMIN)) {
            throw new ResourceForbiddenException("Không có quyền kích hoạt phòng chat");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", id));

        chatRoom.activate();
        chatRoomRepository.save(chatRoom);
        log.info("Chat room activated: {} by user: {}", id, currentUser.getId());
    }

    private ChatRoomResponse mapToResponse(ChatRoom chatRoom) {
        long messageCount = chatMessageRepository.countByChatRoomAndIsDeletedFalse(chatRoom);

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .livestreamId(chatRoom.getLivestreamId())
                .roomType(chatRoom.getRoomType())
                .settings(chatRoom.getSettings())
                .isActive(chatRoom.getIsActive())
                .messageCount((int) messageCount)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
