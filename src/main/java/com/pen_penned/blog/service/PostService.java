package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.PostRequest;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.dto.response.PostDetailsResponse;
import com.pen_penned.blog.dto.response.PostResponse;
import com.pen_penned.blog.model.User;

import java.nio.file.AccessDeniedException;

public interface PostService {


    PostResponse createPost(PostRequest postRequest, User user);

    PageResponse<PostResponse> getAllPosts(Integer pageNumber, Integer pageSize,
                                           String sortBy, String sortOrder);

    PostDetailsResponse getPostById(Long postId);

    PageResponse<PostResponse> getUserPosts(User user, Integer pageNumber,
                                            Integer pageSize, String sortBy, String sortOrder);

    PageResponse<PostResponse> getPostsByUserId(Long userId, Integer pageNumber, Integer pageSize,
                                                String sortBy, String sortOrder);

    PostResponse updatePost(Long postId, PostRequest postRequest) throws AccessDeniedException;

    void deletePost(Long postId) throws AccessDeniedException;

    PostResponse getPostResponseById(Long postId);

    void verifyPostOwnership(Long postId, Long userId) throws AccessDeniedException;
}
