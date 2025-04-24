package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.BookmarkRequest;
import com.pen_penned.blog.dto.response.BookmarkResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;

public interface BookmarkService {

    BookmarkResponse createBookMark(@Valid BookmarkRequest bookmarkRequest, Long userId);

    @Transactional
    void deleteBookmark(Long bookmarkId) throws AccessDeniedException;

    PageResponse<BookmarkResponse> getUserBookmarks(
            Long userId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder
    );

    BookmarkResponse getBookmark(Long bookmarkId) throws AccessDeniedException;

    boolean isBookmarked(Long postId);
}
