package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByLivestreamIdAndRoomType(Long livestreamId, ChatRoom.RoomType roomType);

    List<ChatRoom> findByLivestreamId(Long livestreamId);

    List<ChatRoom> findByRoomTypeAndIsActiveTrue(ChatRoom.RoomType roomType);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.roomType = :roomType AND cr.isActive = true ORDER BY cr.createdAt DESC")
    Page<ChatRoom> findActiveRoomsByType(@Param("roomType") ChatRoom.RoomType roomType, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true ORDER BY cr.createdAt DESC")
    Page<ChatRoom> findAllActiveRooms(Pageable pageable);

    @Query("SELECT COUNT(cr) FROM ChatRoom cr WHERE cr.roomType = :roomType AND cr.isActive = true")
    long countActiveRoomsByType(@Param("roomType") ChatRoom.RoomType roomType);

    boolean existsByLivestreamIdAndRoomType(Long livestreamId, ChatRoom.RoomType roomType);
}
