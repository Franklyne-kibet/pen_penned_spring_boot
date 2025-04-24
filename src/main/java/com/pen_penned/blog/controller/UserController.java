package com.pen_penned.blog.controller;

import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.dto.response.PostResponse;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.service.PostService;
import com.pen_penned.blog.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthUtil authUtil;
    private final PostService postService;

    @GetMapping("/me/posts")
    public ResponseEntity<PageResponse<PostResponse>> getMyPosts(
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_POSTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        User user = authUtil.loggedInUser();
        PageResponse<PostResponse> postResponse = postService
                .getUserPosts(user, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }


    @GetMapping("/{userId}/posts")
    public ResponseEntity<PageResponse<PostResponse>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_POSTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        PageResponse<PostResponse> postResponse = postService.getPostsByUserId(
                userId, pageNumber, pageSize, sortBy, sortOrder
        );
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }
}
