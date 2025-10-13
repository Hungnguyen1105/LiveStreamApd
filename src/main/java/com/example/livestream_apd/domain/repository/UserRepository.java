package com.example.livestream_apd.domain.repository;


import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByStatus(User.UserStatus status);

    @Query("SELECT u FROM User u WHERE u.isOnline = true")
    List<User> findOnlineUsers();

    @Query("SELECT u FROM User u WHERE u.lastSeen < :cutoffTime AND u.isOnline = true")
    List<User> findStaleOnlineUsers(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Query("UPDATE User u SET u.isOnline = false WHERE u.lastSeen < :cutoffTime AND u.isOnline = true")
    int markStaleUsersOffline(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:searchTerm% OR u.username LIKE %:searchTerm%")
    List<User> searchByNameOrUsername(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);

    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Modifying
    @Query("UPDATE User u SET u.followersCount = :count WHERE u.id = :userId")
    void updateFollowersCount(@Param("userId") Long userId, @Param("count") Integer count);

    @Modifying
    @Query("UPDATE User u SET u.followingCount = :count WHERE u.id = :userId")
    void updateFollowingCount(@Param("userId") Long userId, @Param("count") Integer count);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM User u " +
            "JOIN u.following f WHERE u.id = :followerId AND f.id = :followingId")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Query("SELECT u FROM User u JOIN u.followers f WHERE f.id = :userId")
    Page<User> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.following f WHERE f.id = :userId")
    Page<User> findFollowingByUserId(@Param("userId") Long userId, Pageable pageable);

    // Search methods
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "u.status = com.example.livestream_apd.domain.entity.User.UserStatus.ACTIVE")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    // User suggestions
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT f.id FROM User currentUser JOIN currentUser.following f WHERE currentUser.id = :userId) " +
            "AND u.id != :userId AND u.status = com.example.livestream_apd.domain.entity.User.UserStatus.ACTIVE " +
            "ORDER BY u.followersCount DESC, u.createdAt DESC")
    List<User> findUserSuggestions(@Param("userId") Long userId, Pageable pageable);

    default List<User> findUserSuggestions(Long userId, int limit) {
        return findUserSuggestions(userId, PageRequest.of(0, limit));
    }

    // Block/Unblock methods
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM User u " +
            "JOIN u.blockedUsers b WHERE u.id = :blockerId AND b.id = :blockedId")
    boolean isBlocked(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    @Query("SELECT u FROM User u JOIN u.blockedUsers b WHERE u.id = :userId")
    Page<User> findBlockedUsersByUserId(@Param("userId") Long userId, Pageable pageable);

    // Count methods for followers and following
    @Query("SELECT COUNT(f) FROM User u JOIN u.followers f WHERE u.id = :userId")
    Long countFollowers(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM User u JOIN u.following f WHERE u.id = :userId")
    Long countFollowing(@Param("userId") Long userId);

    // Follow/Unfollow operations without lazy loading
    @Modifying
    @Query(value = "INSERT INTO user_follows (follower_id, following_id) VALUES (:followerId, :followingId)", nativeQuery = true)
    void addFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Modifying
    @Query(value = "DELETE FROM user_follows WHERE follower_id = :followerId AND following_id = :followingId", nativeQuery = true)
    void removeFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}


