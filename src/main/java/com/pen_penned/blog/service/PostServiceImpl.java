package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.CommentRequest;
import com.pen_penned.blog.dto.request.PostRequest;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.dto.response.PostDetailsResponse;
import com.pen_penned.blog.dto.response.PostResponse;
import com.pen_penned.blog.exception.ResourceNotFoundException;
import com.pen_penned.blog.model.Post;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.CommentRepository;
import com.pen_penned.blog.repositories.PostRepository;
import com.pen_penned.blog.repositories.UserRepository;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AuthUtil authUtil;

    @Override
    public PostResponse createPost(PostRequest postRequest, User user) {
        // Map DTO to Entity
        Post post = modelMapper.map(postRequest, Post.class);
        post.setAuthor(user);

        // Set published to true
        post.setPublished(true);

        // Add post to user's post list
        List<Post> postList = user.getPosts();
        postList.add(post);
        user.setPosts(postList);

        Post savedPost = postRepository.save(post);

        PostResponse postResponse = modelMapper.map(savedPost, PostResponse.class);

        // Set author details
        postResponse.setAuthorId(user.getId());
        postResponse.setAuthorFirstName(user.getFirstName());
        postResponse.setAuthorLastName(user.getLastName());

        //  Ensure comments is zero
        postResponse.setCommentCount(0);

        return postResponse;
    }


    @Override
    public PageResponse<PostResponse> getAllPosts(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) {

        //  Sort configuration
        Sort sortByAnyOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAnyOrder);

        // Fetch posts
        Page<Post> pagePosts = postRepository.findAll(pageDetails);

        List<Post> posts = pagePosts.getContent();

        // Convert posts to DTOs
        List<PostResponse> postResponses = posts.stream()
                .map(post -> {
                    PostResponse postResponse = modelMapper.map(post, PostResponse.class);
                    postResponse.setCommentCount(commentRepository.getCommentCountByPostId(post.getId()));
                    return postResponse;
                }).toList();

        // Prepare PostResponse
        return new PageResponse<>(
                postResponses,
                pagePosts.getNumber(),
                pagePosts.getSize(),
                pagePosts.getTotalElements(),
                pagePosts.getTotalPages(),
                pagePosts.isLast()
        );
    }

    @Override
    public PostDetailsResponse getPostById(Long postId) {
        // Fetch the post by ID
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        // Map post to DTO
        PostDetailsResponse postDetailsResponse = modelMapper.map(post, PostDetailsResponse.class);

        // Set comment count
        postDetailsResponse.setCommentCount(commentRepository.getCommentCountByPostId(post.getId()));

        // Load comments with author names
        List<CommentRequest> comments = post.getComments().stream()
                .map(comment -> modelMapper.map(comment, CommentRequest.class))
                .toList();

        postDetailsResponse.setComments(comments);

        return postDetailsResponse;
    }


    @Override
    public PageResponse<PostResponse> getUserPosts(
            User user,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) {
        //  Sort configuration
        Sort sortByAnyOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAnyOrder);

        // Get paginated posts for the user
        Page<Post> pagePosts = postRepository.findByAuthor(user, pageDetails);

        List<PostResponse> postResponses = pagePosts.getContent().stream()
                .map(post -> {
                    PostResponse postResponse = modelMapper.map(post, PostResponse.class);
                    postResponse.setCommentCount(post.getComments().size());
                    return postResponse;
                })
                .toList();

        return new PageResponse<>(
                postResponses,
                pagePosts.getNumber(),
                pagePosts.getSize(),
                pagePosts.getTotalElements(),
                pagePosts.getTotalPages(),
                pagePosts.isLast()
        );
    }

    @Override
    public PageResponse<PostResponse> getPostsByUserId(
            Long userId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) {

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Sort and pagination
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Fetch posts by user
        Page<Post> postsPage = postRepository.findByAuthor(user, pageable);

        // Map to PostResponse with comment count
        List<PostResponse> postResponses = postsPage.getContent().stream()
                .map(post -> {
                    PostResponse response = modelMapper.map(post, PostResponse.class);
                    response.setCommentCount(post.getComments().size());
                    return response;
                })
                .toList();

        // Return paginated response
        return new PageResponse<>(
                postResponses,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                postsPage.isLast()
        );
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, PostRequest postRequest) throws AccessDeniedException {
        // Fetch the existing post
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        // Check if the logged-in user is the owner of the post
        User loggedInUser = authUtil.loggedInUser();
        if (!existingPost.getAuthor().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("You do not have permission to update this post.");
        }

        // Update only non-null fields
        if (postRequest.getTitle() != null) existingPost.setTitle(postRequest.getTitle());
        if (postRequest.getContent() != null) existingPost.setContent(postRequest.getContent());
        if (postRequest.getSlug() != null) existingPost.setSlug(postRequest.getSlug());
        if (postRequest.getTags() != null) existingPost.setTags(postRequest.getTags());
        if (postRequest.getCoverImageUrl() != null) existingPost.setCoverImageUrl(postRequest.getCoverImageUrl());
        if (postRequest.getPublished() != null) existingPost.setPublished(postRequest.getPublished());

        // Save updated post
        Post updatedPost = postRepository.save(existingPost);

        // Convert to DTO
        PostResponse postResponse = modelMapper.map(updatedPost, PostResponse.class);
        postResponse.setAuthorFirstName(loggedInUser.getFirstName());
        postResponse.setAuthorLastName(loggedInUser.getLastName());
        postResponse.setCommentCount(commentRepository.getCommentCountByPostId(postId));

        return postResponse;
    }


    @Override
    @Transactional
    public void deletePost(Long postId) throws AccessDeniedException {
        // Fetch the post from the database
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        User loggedInUser = authUtil.loggedInUser();
        if (!post.getAuthor().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this post.");
        }

        // Delete the post from the repository
        postRepository.delete(post);
    }
}
