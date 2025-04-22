package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.CommentDTO;
import com.pen_penned.blog.dto.response.CommentResponse;
import com.pen_penned.blog.model.User;
import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;

public interface CommentService {

    CommentDTO createComment(@Valid CommentDTO commentDTO, User user);

    CommentDTO updateComment(Long commentId, CommentDTO commentDTO) throws AccessDeniedException;

    void deleteComment(Long commentId) throws AccessDeniedException;

    CommentResponse getCommentsByPost(Long postId, Integer pageNumber, Integer pageSize,
                                      String sortBy, String sortOrder);

    CommentDTO getCommentById(Long commentId);
}
