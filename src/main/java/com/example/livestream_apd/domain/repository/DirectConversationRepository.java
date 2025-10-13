package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.DirectConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DirectConversationRepository extends JpaRepository<DirectConversation, Long> {

    @Query("SELECT dc FROM DirectConversation dc " +
           "WHERE (dc.user1.id = :userId OR dc.user2.id = :userId) " +
           "AND ((dc.user1.id = :userId AND dc.user1DeletedAt IS NULL) OR " +
           "     (dc.user2.id = :userId AND dc.user2DeletedAt IS NULL)) " +
           "ORDER BY dc.lastMessageAt DESC, dc.updatedAt DESC")
    Page<DirectConversation> findActiveConversationsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT dc FROM DirectConversation dc " +
           "WHERE ((dc.user1.id = :user1Id AND dc.user2.id = :user2Id) OR " +
           "       (dc.user1.id = :user2Id AND dc.user2.id = :user1Id))")
    Optional<DirectConversation> findByTwoUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("SELECT dc FROM DirectConversation dc " +
           "WHERE dc.id = :conversationId " +
           "AND (dc.user1.id = :userId OR dc.user2.id = :userId) " +
           "AND ((dc.user1.id = :userId AND dc.user1DeletedAt IS NULL) OR " +
           "     (dc.user2.id = :userId AND dc.user2DeletedAt IS NULL))")
    Optional<DirectConversation> findActiveConversationByIdAndUserId(
            @Param("conversationId") Long conversationId, 
            @Param("userId") Long userId);

    @Query("SELECT COUNT(dm) FROM DirectMessage dm " +
           "JOIN dm.conversation dc " +
           "WHERE dc.id = :conversationId " +
           "AND dm.sender.id != :userId " +
           "AND dm.isRead = false " +
           "AND dm.isDeleted = false")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Query("SELECT dc FROM DirectConversation dc " +
           "WHERE (dc.user1.id = :userId OR dc.user2.id = :userId) " +
           "AND ((dc.user1.id = :userId AND dc.user1DeletedAt IS NULL) OR " +
           "     (dc.user2.id = :userId AND dc.user2DeletedAt IS NULL)) " +
           "AND EXISTS (SELECT 1 FROM DirectMessage dm " +
           "           WHERE dm.conversation = dc " +
           "           AND dm.sender.id != :userId " +
           "           AND dm.isRead = false " +
           "           AND dm.isDeleted = false) " +
           "ORDER BY dc.lastMessageAt DESC")
    Page<DirectConversation> findConversationsWithUnreadMessages(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT dc FROM DirectConversation dc " +
           "JOIN dc.user1 u1 " +
           "JOIN dc.user2 u2 " +
           "WHERE (dc.user1.id = :userId OR dc.user2.id = :userId) " +
           "AND ((dc.user1.id = :userId AND dc.user1DeletedAt IS NULL) OR " +
           "     (dc.user2.id = :userId AND dc.user2DeletedAt IS NULL)) " +
           "AND (LOWER(u1.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(u1.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(u2.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(u2.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY dc.lastMessageAt DESC")
    Page<DirectConversation> searchConversations(@Param("userId") Long userId, @Param("query") String query, Pageable pageable);

    @Query("SELECT dc FROM DirectConversation dc " +
           "WHERE (dc.user1.id = :userId OR dc.user2.id = :userId) " +
           "AND ((dc.user1.id = :userId AND dc.isBlockedByUser1 = true) OR " +
           "     (dc.user2.id = :userId AND dc.isBlockedByUser2 = true)) " +
           "ORDER BY dc.updatedAt DESC")
    Page<DirectConversation> findBlockedConversations(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(dc) FROM DirectConversation dc " +
           "WHERE (dc.user1.id = :userId OR dc.user2.id = :userId) " +
           "AND ((dc.user1.id = :userId AND dc.user1DeletedAt IS NULL) OR " +
           "     (dc.user2.id = :userId AND dc.user2DeletedAt IS NULL)) " +
           "AND EXISTS (SELECT 1 FROM DirectMessage dm " +
           "           WHERE dm.conversation = dc " +
           "           AND dm.sender.id != :userId " +
           "           AND dm.isRead = false " +
           "           AND dm.isDeleted = false)")
    Long countConversationsWithUnreadMessages(@Param("userId") Long userId);

    @Query("SELECT dc FROM DirectConversation dc " +
           "WHERE (dc.user1.id = :userId OR dc.user2.id = :userId) " +
           "AND dc.lastMessageAt >= :since " +
           "ORDER BY dc.lastMessageAt DESC")
    Page<DirectConversation> findRecentConversations(
            @Param("userId") Long userId, 
            @Param("since") LocalDateTime since, 
            Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(dc) > 0 THEN true ELSE false END " +
           "FROM DirectConversation dc " +
           "WHERE dc.id = :conversationId " +
           "AND (dc.user1.id = :userId OR dc.user2.id = :userId)")
    Boolean isUserParticipantInConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN " +
           "((dc.user1.id = :userId AND dc.isBlockedByUser1 = true) OR " +
           " (dc.user2.id = :userId AND dc.isBlockedByUser2 = true)) " +
           "THEN true ELSE false END " +
           "FROM DirectConversation dc " +
           "WHERE dc.id = :conversationId")
    Boolean isConversationBlockedByUser(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
