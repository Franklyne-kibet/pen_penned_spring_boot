package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.BookmarkRequest;
import com.pen_penned.blog.dto.response.BookmarkResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.exception.ResourceNotFoundException;
import com.pen_penned.blog.model.Bookmark;
import com.pen_penned.blog.model.Post;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.BookmarkRepository;
import com.pen_penned.blog.repositories.PostRepository;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final ModelMapper modelMapper;
    private final BookmarkRepository bookmarkRepository;
    private final AuthUtil authUtil;
    private final PostRepository postRepository;


    @Override
    public BookmarkResponse createBookMark(BookmarkRequest bookmarkRequest, Long userId) {
        User user = authUtil.loggedInUser();

        Post post = postRepository.findById(bookmarkRequest.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", bookmarkRequest.getPostId()));

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .post(post)
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        return modelMapper.map(savedBookmark, BookmarkResponse.class);
    }


    @Override
    public PageResponse<BookmarkResponse> getUserBookmarks(Long userId, Integer pageNumber,
                                                           Integer pageSize, String sortBy,
                                                           String sortOrder) {
        User user = authUtil.loggedInUser();

        // Sort configuration
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Fetch Bookmarks
        Page<Bookmark> bookmarkPage = bookmarkRepository.findBookmarksByUserId(user.getId(), pageDetails);

        // Convert bookmarks to DTOs
        List<BookmarkResponse> bookmarkResponses = bookmarkPage
                .getContent()
                .stream()
                .map(bookmark -> modelMapper.map(bookmark, BookmarkResponse.class))
                .toList();

        return new PageResponse<>(
                bookmarkResponses,
                bookmarkPage.getNumber(),
                bookmarkPage.getSize(),
                bookmarkPage.getTotalElements(),
                bookmarkPage.getTotalPages(),
                bookmarkPage.isLast()
        );
    }


    @Override
    public BookmarkResponse getBookmark(Long bookmarkId) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", bookmarkId));

        // Check if the user owns this bookmark
        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this bookmark.");
        }

        return modelMapper.map(bookmark, BookmarkResponse.class);
    }


    @Override
    public boolean isBookmarked(Long postId) {
        User user = authUtil.loggedInUser();

        return bookmarkRepository.existsByUserIdAndPostId(user.getId(), postId);
    }


    @Override
    @Transactional
    public void deleteBookmark(Long bookmarkId) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        // Find the bookmark
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "bookmarkId", bookmarkId));

        // Check if the user owns this bookmark
        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this bookmark.");
        }

        // Remove bookmark from user's list
        // user.getBookmarks().remove(bookmark);

        // Delete from repository
        bookmarkRepository.delete(bookmark);
    }


    /* @Override
    @Transactional
    public BookmarkDto.Response addBookmarkToFolder(Long bookmarkId, Long folderId) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", bookmarkId));

        // Check if user owns this bookmark
        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this bookmark.");
        }

        // In a real implementation, you would fetch the folder and validate ownership
        // For simplicity, we'll just mention the update

        // Update folder and save
        // bookmark.setFolder(folder);
        Bookmark updatedBookmark = bookmarkRepository.save(bookmark);

        return modelMapper.map(updatedBookmark, BookmarkDto.Response.class);
    }

    @Override
    @Transactional
    public BookmarkDto.Response addNoteToBookmark(Long bookmarkId, String note) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", bookmarkId));

        // Check if user owns this bookmark
        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this bookmark.");
        }

        // Update note and save
        bookmark.setNotes(note);
        Bookmark updatedBookmark = bookmarkRepository.save(bookmark);

        return modelMapper.map(updatedBookmark, BookmarkDto.Response.class);
    } */
}
