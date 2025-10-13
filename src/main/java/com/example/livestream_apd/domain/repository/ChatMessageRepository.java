package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.ChatMessage;
import com.example.livestream_apd.domain.entity.ChatRoom;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByChatRoomAndIsDeletedFalseOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);

    Page<ChatMessage> findByChatRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    List<ChatMessage> findByChatRoomAndCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtAsc(
            ChatRoom chatRoom, LocalDateTime after);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.isDeleted = false " +
           "AND cm.createdAt > :after ORDER BY cm.createdAt ASC")
    List<ChatMessage> findRecentMessages(@Param("roomId") Long roomId, @Param("after") LocalDateTime after);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user = :user AND cm.isDeleted = false " +
           "ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByUserAndIsDeletedFalse(User user, Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom AND cm.messageType = :messageType " +
           "AND cm.isDeleted = false ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByChatRoomAndMessageTypeAndIsDeletedFalse(
            @Param("chatRoom") ChatRoom chatRoom, 
            @Param("messageType") ChatMessage.MessageType messageType, 
            Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom AND cm.content LIKE %:keyword% " +
           "AND cm.isDeleted = false ORDER BY cm.createdAt DESC")
    Page<ChatMessage> searchInChatRoom(
            @Param("chatRoom") ChatRoom chatRoom, 
            @Param("keyword") String keyword, 
            Pageable pageable);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom AND cm.isDeleted = false")
    long countByChatRoomAndIsDeletedFalse(@Param("chatRoom") ChatRoom chatRoom);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.user = :user AND cm.createdAt >= :since")
    long countByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isDeleted = true WHERE cm.chatRoom = :chatRoom")
    void softDeleteAllInRoom(@Param("chatRoom") ChatRoom chatRoom);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isDeleted = true WHERE cm.user = :user")
    void softDeleteAllByUser(@Param("user") User user);

    List<ChatMessage> findByParentMessage(ChatMessage parentMessage);

    boolean existsByChatRoomAndUserAndCreatedAtAfter(ChatRoom chatRoom, User user, LocalDateTime after);
}
