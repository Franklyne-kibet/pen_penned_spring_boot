package com.pen_penned.blog.controller;

import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.dto.request.BookmarkDTO;
import com.pen_penned.blog.dto.response.BookmarkResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.service.BookmarkService;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final AuthUtil authUtil;
    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<BookmarkResponse> createBookmark(@Valid @RequestBody BookmarkDTO bookmarkDTO) {
        User user = authUtil.loggedInUser();
        BookmarkResponse bookmarkResponse = bookmarkService.createBookMark(bookmarkDTO, user.getId());
        return new ResponseEntity<>(bookmarkResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookmarkResponse>> getUserBookmarks(
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_COMMENTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        User user = authUtil.loggedInUser();
        PageResponse<BookmarkResponse> bookmarks = bookmarkService.getUserBookmarks(user.getId(), pageNumber,
                pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(bookmarks, HttpStatus.OK);
    }

    @GetMapping("/{bookmarkId}")
    public ResponseEntity<BookmarkResponse> getBookmark(@PathVariable Long bookmarkId) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        BookmarkResponse bookmarkResponse = bookmarkService.getBookmark(bookmarkId);

        return new ResponseEntity<>(bookmarkResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long bookmarkId) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        bookmarkService.deleteBookmark(bookmarkId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{postId}")
    public ResponseEntity<Boolean> isBookmarked(@PathVariable Long postId) {
        User user = authUtil.loggedInUser();
        boolean isBookmarked = bookmarkService.isBookmarked(postId);
        return new ResponseEntity<>(isBookmarked, HttpStatus.OK);
    }
}
