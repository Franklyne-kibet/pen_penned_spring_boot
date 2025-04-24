package com.pen_penned.blog.controller;

import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.dto.request.CommentRequest;
import com.pen_penned.blog.dto.response.CommentResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.service.CommentService;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final AuthUtil authUtil;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest commentRequest) {
        User user = authUtil.loggedInUser();
        CommentResponse savedComment = commentService.createComment(commentRequest, user);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        CommentResponse commentResponse = commentService.getCommentById(commentId);
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PageResponse<CommentResponse>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_COMMENTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        PageResponse<CommentResponse> commentResponse = commentService.getCommentsByPost(postId,
                pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }


    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateCommentById(
            @PathVariable Long commentId, @Valid @RequestBody CommentRequest commentRequest) throws AccessDeniedException {
        CommentResponse updatedComment = commentService.updateComment(commentId, commentRequest);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }


    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) throws AccessDeniedException {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
