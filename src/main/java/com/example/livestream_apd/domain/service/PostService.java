package com.example.livestream_apd.domain.service;


import com.example.livestream_apd.application.dto.request.CreatePostRequest;
import com.example.livestream_apd.application.dto.request.UpdatePostRequest;
import com.example.livestream_apd.application.dto.response.PostDetailResponse;
import com.example.livestream_apd.application.dto.response.PostResponse;
import com.example.livestream_apd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    PostResponse createPost (User currentUser, CreatePostRequest createPostRequest);
    Page<PostResponse> getTimelinePosts (User currentUser, Pageable pageable);
    PostDetailResponse getPostById(User currentUser, Long postId);
    PostResponse updatePost(User currentUser, Long postId, UpdatePostRequest updatePostRequest);
    void deletePost(User currentUser, Long postId);

    PostResponse likePost(User currentUser, Long postId);
    PostResponse unlikePost(User currentUser, Long postId);
    PostResponse sharePost(User currentUser, Long postId, String platforms);
    Page<PostResponse> getTrendingPosts(User currentUser, Pageable pageable);
    Page <PostResponse> getUserPosts(User currentUser,Long userId, Pageable pageable);

}
