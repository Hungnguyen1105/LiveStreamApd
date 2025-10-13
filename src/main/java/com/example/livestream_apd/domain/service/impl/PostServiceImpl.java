package com.example.livestream_apd.domain.service.impl;

import com.example.livestream_apd.application.dto.response.*;
import com.example.livestream_apd.application.dto.request.CreatePostRequest;
import com.example.livestream_apd.application.dto.request.UpdatePostRequest;
import com.example.livestream_apd.domain.entity.*;
import com.example.livestream_apd.domain.repository.*;
import com.example.livestream_apd.domain.service.PostService;
import com.example.livestream_apd.utils.exceptions.ResourceForbiddenException;
import com.example.livestream_apd.utils.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;

    @Override
    @Transactional
    public PostResponse createPost(User currentUser, CreatePostRequest createPostRequest){
        Post post = Post
                .builder()
                .user(currentUser)
                .caption(createPostRequest.getCaption())
                .mediaUrls(createPostRequest.getMediaUrls())
                .location(createPostRequest.getLocation())
                .tags(createPostRequest.getTags())
                .isPublic(createPostRequest.isPublic())
                .build();
        Post savedPost = postRepository.save(post);
        return mapPostToResponse(savedPost, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getTimelinePosts (User currentUser, Pageable pageable){
        Page<Post> posts = postRepository.findTimelineForUser(currentUser, pageable);
        return posts.map(post -> mapPostToResponse(post, currentUser));
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostById(User currentUser, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("post","id",postId));
        if (!post.getIsPublic() && !post.getUser().getId().equals(currentUser.getId())){
            throw new ResourceForbiddenException("Không có quyền truy cập");
        }
        Page<Comment> comments = commentRepository
                    .findByPostAndParentCommentIsNullOrderByIsPinnedDescCreatedAtDesc
                    (post,Pageable.ofSize(10));
        List<CommentResponse> commentResponses = comments.getContent().stream()
                .map(comment -> mapToCommentResponse(comment,currentUser)).collect(Collectors.toList());
        boolean isLiked = likeRepository.existsByUserAndPost(currentUser,post);
        boolean isShared = shareRepository.findByUserAndPost(currentUser,post).isPresent();
        return PostDetailResponse
                .builder()
                .id(post.getId())
                .user(mapUserToResponse(post.getUser()))
                .caption(post.getCaption())
                .mediaUrls(post.getMediaUrls())
                .location(post.getLocation())
                .tags(post.getTags())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .isPublic(post.getIsPublic())
                .isLiked(isLiked)
                .isShared(isShared)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public PostResponse updatePost(User currentUser, Long postId, UpdatePostRequest updatePostRequest){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("post","id",postId));
        if (!post.getUser().getId().equals(currentUser.getId())){
            throw new ResourceForbiddenException("Không có quyền sửa post này");
        }
        if (updatePostRequest.getCaption() != null){
            post.setCaption(updatePostRequest.getCaption());
        }
        if (updatePostRequest.getMediaUrls() != null){
            post.setMediaUrls(updatePostRequest.getMediaUrls());
        }
        if (updatePostRequest.getLocation() != null){
            post.setLocation(updatePostRequest.getLocation());
        }
        if (updatePostRequest.getTags() != null){
            post.setTags(updatePostRequest.getTags());
        }
        if (updatePostRequest.getIsPublic() != null){
            post.setIsPublic(updatePostRequest.getIsPublic());
        }
        Post updatedPost = postRepository.save(post);
        return mapPostToResponse(updatedPost, currentUser);
    }

    @Override
    @Transactional
    public void deletePost(User currentUser, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("post","id",postId));
        if (!post.getUser().getId().equals(currentUser.getId())){
            throw new ResourceForbiddenException("Không có quyền xoá bài post này");
        }
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public PostResponse likePost(User currentUser, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("post","id",postId));
        if (!post.getUser().getId().equals(currentUser.getId()) && !post.getIsPublic()){
            throw new ResourceForbiddenException("Không có quyền like bài viết");
        }
        if (!likeRepository.existsByUserAndPost(currentUser,post)){
            Like like = Like
                    .builder()
                    .post(post)
                    .user(currentUser)
                    .build();
            likeRepository.save(like);
            post.setLikeCount(post.getLikeCount()+1);
            postRepository.save(post);
        }
        return mapPostToResponse(post, currentUser);
    }

    @Override
    @Transactional
    public  PostResponse unlikePost(User currentUser, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("post","id",postId));
        Like like = likeRepository.findByUserAndPost(currentUser,post).orElseThrow(() -> new ResourceNotFoundException("post","id",postId));
        likeRepository.delete(like);
        if (post.getLikeCount() > 0){
            post.setLikeCount(post.getLikeCount()-1);
            postRepository.save(post);
        }
        return mapPostToResponse(post, currentUser);
    }

    @Override
    @Transactional
    public  PostResponse sharePost(User currentUser, Long postId, String platforms){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("post","id",postId));
        if (!post.getIsPublic()){
            throw new ResourceForbiddenException("không thể chia sẻ bài viết không công khai");
        }
        Share.Platform platform = Share.Platform.INTERNAL;
        if (platform != null){
            try{
                platform = Share.Platform.valueOf(platforms.toUpperCase());
            } catch (IllegalArgumentException e){
                log.warn(e.getMessage());
            }
        }
        Share share = Share
                .builder()
                .user(currentUser)
                .post(post)
                .platform(platform)
                .build();
        shareRepository.save(share);
        post.setShareCount(post.getShareCount()+1);
        postRepository.save(post);
        return mapPostToResponse(post, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getTrendingPosts(User currentUser, Pageable pageable){
        Page<Post> posts = postRepository.findTrendingPosts(pageable);
        return posts.map(post -> mapPostToResponse(post, currentUser));
    }

    @Override
    @Transactional
    public Page<PostResponse> getUserPosts(User currentUser,Long userId, Pageable pageable){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user","id",userId));
        Page<Post> posts;
        if (user.getId().equals(currentUser.getId())){
            posts = postRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        } else {
            posts = postRepository.findByUserAndIsPublicTrueOrderByCreatedAtDesc(user, pageable);
        }
        return posts.map(post -> mapPostToResponse(post, currentUser));
    }

    private PostResponse mapPostToResponse(Post post, User currentUser){
        boolean isLiked = likeRepository.existsByUserAndPost(currentUser, post);
        boolean isShared = shareRepository.findByUserAndPost(currentUser,post).isPresent();
        return PostResponse
                .builder()
                .id(post.getId())
                .user(mapUserToResponse(post.getUser()))
                .caption(post.getCaption())
                .mediaUrls(post.getMediaUrls())
                .locations(post.getLocation())
                .tags(post.getTags())
                .isLiked(isLiked)
                .isShared(isShared)
                .isPublic(post.getIsPublic())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private UserSummaryResponse mapUserToResponse(User user){
        try{
            UserSummaryResponse userSummaryResponse = UserSummaryResponse
                    .builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .isVerified(user.getIsVerified())
                    .build();
            return userSummaryResponse;
        } catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    private CommentResponse mapToCommentResponse(Comment comment, User currentUser){
        boolean isLiked = false;
        for (Like like : comment.getLikes()) {
            if (like.getUser().getId().equals(currentUser.getId())) {
                isLiked = true;
                break;
            }
        }
            List<CommentResponse> replies = new ArrayList<>();
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()){
                replies = comment.getReplies().stream()
                        .map(reply -> mapToCommentResponse(reply,currentUser)).collect(Collectors.toList());
            }
            return CommentResponse
                    .builder()
                    .id(comment.getId())
                    .user(mapUserToResponse(comment.getUser()))
                    .content(comment.getContent())
                    .likeCount(comment.getLikeCount())
                    .isPinned(comment.getIsPinned())
                    .isLiked(isLiked)
                    .createdAt(comment.getCreatedAt())
                    .replies(replies)
                    .build();
        }
}
