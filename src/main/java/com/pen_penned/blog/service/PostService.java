package com.pen_penned.blog.service;

import com.pen_penned.blog.model.User;
import com.pen_penned.blog.payload.PostDTO;
import com.pen_penned.blog.payload.PostDetailsDTO;
import com.pen_penned.blog.payload.PostResponse;
import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface PostService {


    PostDTO createPost(@Valid PostDTO postDTO, User user);

    PostResponse getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    PostDetailsDTO getPostById(Long postId);

    List<PostDTO> getUserPosts(User user);

    PostDTO updatePost(Long postId, @Valid PostDTO postDTO) throws AccessDeniedException;

    void deletePost(Long postId) throws AccessDeniedException;

}
