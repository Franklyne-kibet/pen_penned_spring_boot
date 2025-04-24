package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.CommentRequest;
import com.pen_penned.blog.dto.response.CommentResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.model.User;
import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;

public interface CommentService {

    CommentResponse createComment(@Valid CommentRequest commentRequest, User user);

    CommentResponse updateComment(Long commentId, CommentRequest commentRequest) throws AccessDeniedException;

    void deleteComment(Long commentId) throws AccessDeniedException;

    PageResponse<CommentResponse> getCommentsByPost(Long postId, Integer pageNumber, Integer pageSize,
                                                    String sortBy, String sortOrder);

    CommentResponse getCommentById(Long commentId);
}
