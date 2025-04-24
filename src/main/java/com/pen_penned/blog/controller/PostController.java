package com.pen_penned.blog.controller;

import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.dto.request.PostRequest;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.dto.response.PostDetailsResponse;
import com.pen_penned.blog.dto.response.PostResponse;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.service.PostService;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final AuthUtil authUtil;
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest) {
        User user = authUtil.loggedInUser();
        PostResponse savedPost = postService.createPost(postRequest, user);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> getAllPosts(
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_POSTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        PageResponse<PostResponse> postResponse = postService.getAllPosts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailsResponse> getPostById(@PathVariable Long postId) {
        PostDetailsResponse postDetailsResponse = postService.getPostById(postId);
        return new ResponseEntity<>(postDetailsResponse, HttpStatus.OK);
    }


    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePostById(
            @PathVariable Long postId, @Valid @RequestBody PostRequest postRequest) throws AccessDeniedException {
        PostResponse updatedPost = postService.updatePost(postId, postRequest);
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) throws AccessDeniedException {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

}
