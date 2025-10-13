package com.example.livestream_apd.domain.repository;



import com.example.livestream_apd.domain.entity.Like;
import com.example.livestream_apd.domain.entity.Post;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    void deleteByUserAndPost(User user, Post post);
}
