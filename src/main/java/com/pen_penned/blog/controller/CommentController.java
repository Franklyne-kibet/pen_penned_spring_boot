package com.pen_penned.blog.controller;

import com.pen_penned.blog.dto.request.CommentDTO;
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

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) throws AccessDeniedException {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
