package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.BookmarkRequest;
import com.pen_penned.blog.dto.response.BookmarkResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.exception.ResourceNotFoundException;
import com.pen_penned.blog.model.Bookmark;
import com.pen_penned.blog.model.BookmarkFolder;
import com.pen_penned.blog.model.Post;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.BookmarkFolderRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final AuthUtil authUtil;
    private final ModelMapper modelMapper;
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkFolderRepository bookmarkFolderRepository;
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

        // Convert to response
        BookmarkResponse bookmarkResponse = modelMapper.map(savedBookmark, BookmarkResponse.class);

        // If the post title is available, set it
        if (post.getTitle() != null) {
            bookmarkResponse.setPostTitle(post.getTitle());
        }

        return bookmarkResponse;
    }


    @Override
    public PageResponse<BookmarkResponse> getUserBookmarks(
            Long userId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
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
                .map(bookmark -> {
                    BookmarkResponse bookmarkResponse = modelMapper.map(bookmark, BookmarkResponse.class);

                    // Add post title if available
                    if (bookmark.getPost().getTitle() != null) {
                        bookmarkResponse.setPostTitle(bookmark.getPost().getTitle());
                    }

                    // Add folder information
                    List<BookmarkFolder> bookmarkFolders = bookmarkFolderRepository.findByBookmarkId(bookmark.getId());
                    Set<BookmarkResponse.FolderMinimalResponse> folders = bookmarkFolders.stream()
                            .map(bf -> BookmarkResponse.FolderMinimalResponse.builder()
                                    .id(bf.getFolder().getId())
                                    .name(bf.getFolder().getName())
                                    .build())
                            .collect(Collectors.toSet());
                    bookmarkResponse.setFolders(folders);

                    return bookmarkResponse;

                })
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

        // First delete all bookmark-folder associations
        List<BookmarkFolder> bookmarkFolders = bookmarkFolderRepository.findByBookmarkId(bookmarkId);
        bookmarkFolderRepository.deleteAll(bookmarkFolders);

        // Delete from repository
        bookmarkRepository.delete(bookmark);
    }
}
