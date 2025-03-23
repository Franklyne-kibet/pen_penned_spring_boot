package com.pen_penned.blog.controller;

import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.payload.CommentResponse;
import com.pen_penned.blog.payload.PostDTO;
import com.pen_penned.blog.payload.PostDetailsDTO;
import com.pen_penned.blog.payload.PostResponse;
import com.pen_penned.blog.service.CommentService;
import com.pen_penned.blog.service.PostService;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final AuthUtil authUtil;
    private final PostService postService;
    private final CommentService commentService;

    @PostMapping("/posts")
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostDTO postDTO) {
        User user = authUtil.loggedInUser();
        PostDTO savedPostDTO = postService.createPost(postDTO, user);
        return new ResponseEntity<>(savedPostDTO, HttpStatus.CREATED);
    }

    @GetMapping("/posts")
    public ResponseEntity<PostResponse> getAllPosts(
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_POSTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        PostResponse postResponse = postService.getAllPosts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDetailsDTO> getPostById(@PathVariable Long postId) {
        PostDetailsDTO postDTO = postService.getPostById(postId);
        return new ResponseEntity<>(postDTO, HttpStatus.OK);
    }

    @GetMapping("/posts/{postId}/comments")
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


    @GetMapping("/users/posts")
    public ResponseEntity<List<PostDTO>> getUserPosts() {
        User user = authUtil.loggedInUser();
        List<PostDTO> postList = postService.getUserPosts(user);
        return new ResponseEntity<>(postList, HttpStatus.OK);
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> updatePostById(
            @PathVariable Long postId, @Valid @RequestBody PostDTO postDTO) throws AccessDeniedException {
        PostDTO updatedPost = postService.updatePost(postId, postDTO);
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) throws AccessDeniedException {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

}
