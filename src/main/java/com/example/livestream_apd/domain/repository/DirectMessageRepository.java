package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.isDeleted = false " +
           "ORDER BY dm.createdAt DESC")
    Page<DirectMessage> findByConversationIdAndNotDeleted(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.isDeleted = false " +
           "AND dm.createdAt > :after " +
           "ORDER BY dm.createdAt ASC")
    List<DirectMessage> findByConversationIdAfterTime(
            @Param("conversationId") Long conversationId, 
            @Param("after") LocalDateTime after);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.isDeleted = false " +
           "AND LOWER(dm.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY dm.createdAt DESC")
    Page<DirectMessage> searchInConversation(
            @Param("conversationId") Long conversationId, 
            @Param("query") String query, 
            Pageable pageable);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.messageType IN :mediaTypes " +
           "AND dm.isDeleted = false " +
           "ORDER BY dm.createdAt DESC")
    Page<DirectMessage> findMediaMessages(
            @Param("conversationId") Long conversationId, 
            @Param("mediaTypes") List<DirectMessage.MessageType> mediaTypes, 
            Pageable pageable);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.id = :messageId " +
           "AND dm.conversation.id = :conversationId " +
           "AND dm.isDeleted = false")
    Optional<DirectMessage> findByIdAndConversationId(
            @Param("messageId") Long messageId, 
            @Param("conversationId") Long conversationId);

    @Query("SELECT COUNT(dm) FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.sender.id != :userId " +
           "AND dm.isRead = false " +
           "AND dm.isDeleted = false")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE DirectMessage dm SET dm.isRead = true, dm.readAt = :readAt " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.sender.id != :userId " +
           "AND dm.isRead = false " +
           "AND dm.isDeleted = false")
    int markAllAsRead(
            @Param("conversationId") Long conversationId, 
            @Param("userId") Long userId, 
            @Param("readAt") LocalDateTime readAt);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.isDeleted = false " +
           "ORDER BY dm.createdAt DESC " +
           "LIMIT 1")
    Optional<DirectMessage> findLastMessage(@Param("conversationId") Long conversationId);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.sender.id = :senderId " +
           "AND dm.createdAt >= :since " +
           "AND dm.isDeleted = false")
    List<DirectMessage> findRecentMessagesBySender(
            @Param("conversationId") Long conversationId, 
            @Param("senderId") Long senderId, 
            @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(dm) FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.isDeleted = false")
    Long countMessagesByConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.replyToMessage.id = :originalMessageId " +
           "AND dm.isDeleted = false " +
           "ORDER BY dm.createdAt ASC")
    List<DirectMessage> findReplies(@Param("originalMessageId") Long originalMessageId);

    @Query("SELECT CASE WHEN COUNT(dm) > 0 THEN true ELSE false END " +
           "FROM DirectMessage dm " +
           "WHERE dm.id = :messageId " +
           "AND dm.sender.id = :userId " +
           "AND dm.isDeleted = false")
    Boolean isMessageOwnedByUser(@Param("messageId") Long messageId, @Param("userId") Long userId);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.createdAt BETWEEN :startDate AND :endDate " +
           "AND dm.isDeleted = false " +
           "ORDER BY dm.createdAt ASC")
    List<DirectMessage> findMessagesBetweenDates(
            @Param("conversationId") Long conversationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT dm.messageType FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.isDeleted = false")
    List<DirectMessage.MessageType> findMessageTypesInConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT dm FROM DirectMessage dm " +
           "WHERE dm.conversation.id = :conversationId " +
           "AND dm.isDeleted = false " +
           "AND dm.createdAt < :before " +
           "ORDER BY dm.createdAt DESC")
    Page<DirectMessage> findMessagesBefore(
            @Param("conversationId") Long conversationId, 
            @Param("before") LocalDateTime before, 
            Pageable pageable);
}
