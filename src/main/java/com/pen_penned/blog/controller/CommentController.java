package com.pen_penned.blog.controller;

import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.dto.request.CommentDTO;
import com.pen_penned.blog.dto.response.CommentResponse;
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

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long commentId) {
        CommentDTO commentDTO = commentService.getCommentById(commentId);
        return new ResponseEntity<>(commentDTO, HttpStatus.OK);
    }

    @GetMapping("/comments/posts/{postId}")
    public ResponseEntity<CommentResponse> getPostComments(
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


    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateCommentById(
            @PathVariable Long commentId, @Valid @RequestBody CommentDTO commentDTO) throws AccessDeniedException {
        CommentDTO updatedComment = commentService.updateComment(commentId, commentDTO);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) throws AccessDeniedException {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
