package com.pen_penned.blog.controller;

import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.payload.CommentDTO;
import com.pen_penned.blog.payload.CommentResponse;
import com.pen_penned.blog.service.CommentService;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final AuthUtil authUtil;
    private final CommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        User user = authUtil.loggedInUser();
        CommentDTO savedCommentDTO = commentService.createComment(commentDTO, user);
        return new ResponseEntity<>(savedCommentDTO, HttpStatus.CREATED);
    }

    @GetMapping("/comments/posts/{postId}")
    public ResponseEntity<CommentResponse> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_COMMENTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        CommentResponse commentResponse = (CommentResponse) commentService.getCommentsByPost(postId,
                pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) throws AccessDeniedException {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
