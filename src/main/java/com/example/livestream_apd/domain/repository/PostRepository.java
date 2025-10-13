package com.example.livestream_apd.domain.repository;


import com.example.livestream_apd.domain.entity.Post;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Post> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByUserAndIsPublicTrueOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isPublic = true AND (p.user = ?1 OR p.user IN " +
            "(SELECT f.following FROM User u JOIN u.following f WHERE u = ?1)) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findTimelineForUser(User user, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isPublic = true " +
            "ORDER BY (p.likeCount * 3 + p.commentCount * 2 + p.shareCount) DESC, p.createdAt DESC")
    Page<Post> findTrendingPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isPublic = true AND " +
            "(LOWER(p.caption) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
            "EXISTS (SELECT t FROM p.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', ?1, '%')))) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> searchPosts(String query, Pageable pageable);
}
