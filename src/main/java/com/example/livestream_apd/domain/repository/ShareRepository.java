package com.example.livestream_apd.domain.repository;


import com.example.livestream_apd.domain.entity.Post;
import com.example.livestream_apd.domain.entity.Share;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {

    int countByPost(Post post);

    Optional<Share> findByUserAndPost(User user, Post post);
}
