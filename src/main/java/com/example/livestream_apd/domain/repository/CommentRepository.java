package com.example.livestream_apd.domain.repository;

import com.example.livestream_apd.domain.entity.Comment;
import com.example.livestream_apd.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostAndParentCommentIsNullOrderByIsPinnedDescCreatedAtDesc(Post post, Pageable pageable);

    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
}
