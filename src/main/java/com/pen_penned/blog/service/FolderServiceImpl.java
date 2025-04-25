package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.BookmarkFolderRequest;
import com.pen_penned.blog.dto.request.FolderRequest;
import com.pen_penned.blog.dto.response.BookmarkResponse;
import com.pen_penned.blog.dto.response.FolderResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.exception.ResourceAlreadyExistsException;
import com.pen_penned.blog.exception.ResourceNotFoundException;
import com.pen_penned.blog.model.Bookmark;
import com.pen_penned.blog.model.BookmarkFolder;
import com.pen_penned.blog.model.Folder;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.BookmarkFolderRepository;
import com.pen_penned.blog.repositories.BookmarkRepository;
import com.pen_penned.blog.repositories.FolderRepository;
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
public class FolderServiceImpl implements FolderService {

    private final ModelMapper modelMapper;
    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkFolderRepository bookmarkFolderRepository;
    private final AuthUtil authUtil;


    @Override
    @Transactional
    public FolderResponse createFolder(FolderRequest folderRequest) {
        User user = authUtil.loggedInUser();

        // Check if folder with same name exists for this user
        if (folderRepository.existsByNameAndUserId(folderRequest.name(), user.getId())) {
            throw new ResourceAlreadyExistsException("Folder", "name", folderRequest.name());
        }

        Folder folder = Folder.builder()
                .name(folderRequest.name())
                .description(folderRequest.description())
                .user(user)
                .build();

        Folder savedFolder = folderRepository.save(folder);

        FolderResponse folderResponse = modelMapper.map(savedFolder, FolderResponse.class);
        folderResponse.setUserId(user.getId());
        folderResponse.setBookmarkCount(0);

        return folderResponse;
    }

    @Override
    public FolderResponse getFolder(Long folderId) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        // Check if user owns this folder
        if (!folder.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this folder");
        }

        FolderResponse folderResponse = modelMapper.map(folder, FolderResponse.class);
        folderResponse.setUserId(user.getId());
        folderResponse.setBookmarkCount(folderRepository.countBookmarksInFolder(folderId));

        return folderResponse;
    }

    @Override
    @Transactional
    public FolderResponse updateFolder(Long folderId, FolderRequest folderRequest) throws AccessDeniedException {

        User user = authUtil.loggedInUser();

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        // Check if user owns this folder
        if (!folder.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to update this folder");
        }

        // Check if new name already exists for this user (if name is being changed)
        if (!folder.getName().equals(folderRequest.name()) &&
                folderRepository.existsByNameAndUserId(folderRequest.name(), user.getId())) {
            throw new ResourceAlreadyExistsException("Folder", "id", folderRequest.name());
        }

        folder.setName(folderRequest.name());
        folder.setDescription(folderRequest.description());

        Folder updatedFolder = folderRepository.save(folder);

        FolderResponse folderResponse = modelMapper.map(updatedFolder, FolderResponse.class);
        folderResponse.setUserId(user.getId());
        folderResponse.setBookmarkCount(folderRepository.countBookmarksInFolder(folderId));

        return folderResponse;
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        // Check if user owns this folder
        if (!folder.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this folder");
        }

        folderRepository.delete(folder);
    }

    @Override
    public PageResponse<FolderResponse> getUserFolders(
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

        // Fetch folders
        Page<Folder> folderPage = folderRepository.findByUserId(user.getId(), pageDetails);

        // Convert to DTOs
        List<FolderResponse> folderResponses = folderPage
                .getContent()
                .stream()
                .map(folder -> {
                    FolderResponse folderResponse = modelMapper.map(folder, FolderResponse.class);
                    folderResponse.setUserId(user.getId());
                    folderResponse.setBookmarkCount(folderRepository.countBookmarksInFolder(folder.getId()));
                    return folderResponse;
                })
                .toList();

        return new PageResponse<>(
                folderResponses,
                folderPage.getNumber(),
                folderPage.getSize(),
                folderPage.getTotalElements(),
                folderPage.getTotalPages(),
                folderPage.isLast()
        );
    }

    @Override
    @Transactional
    public void addBookmarkToFolders(BookmarkFolderRequest bookmarkFolderRequest) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        // Verify bookmark exists and belongs to user
        Bookmark bookmark = bookmarkRepository.findById(bookmarkFolderRequest.bookmarkId())
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", bookmarkFolderRequest.bookmarkId()));

        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this bookmark");
        }

        // For each folder ID
        for (Long folderId : bookmarkFolderRequest.folderIds()) {
            // Verify folder exists and belongs to user
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

            if (!folder.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to access folder with id: " + folderId);
            }

            // Check if bookmark is already in this folder
            if (bookmarkFolderRepository.findByBookmarkIdAndFolderId(bookmark.getId(), folder.getId()).isEmpty()) {
                // Create association
                BookmarkFolder bookmarkFolder = BookmarkFolder.builder()
                        .bookmark(bookmark)
                        .folder(folder)
                        .build();

                bookmarkFolderRepository.save(bookmarkFolder);
            }
        }
    }

    @Override
    @Transactional
    public void removeBookmarkFromFolder(Long bookmarkId, Long folderId) throws AccessDeniedException {
        User user = authUtil.loggedInUser();

        // verify bookmark exists and belongs to user
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", bookmarkId));

        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this bookmark");
        }

        // Verify folder exists and belongs to user
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this folder");
        }

        // Find and delete the association
        BookmarkFolder bookmarkFolder = bookmarkFolderRepository.findByBookmarkIdAndFolderId(bookmarkId, folderId)
                .orElseThrow(() -> new ResourceNotFoundException("BookmarkFolder", "bookmarkId and folderId",
                        bookmarkId + " and " + folderId));

        bookmarkFolderRepository.delete(bookmarkFolder);
    }

    @Override
    public List<FolderResponse> getBookmarkFolders(Long bookmarkId) {
        User user = authUtil.loggedInUser();

        // Verify bookmark exists and belongs to user
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", bookmarkId));

        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to access this bookmark");
        }

        // Get folders that contain this bookmark
        List<Folder> folders = folderRepository.findByBookmarkId(bookmarkId);

        return folders.stream()
                .map(folder -> {
                    FolderResponse folderResponse = modelMapper.map(folder, FolderResponse.class);
                    folderResponse.setUserId(user.getId());
                    folderResponse.setBookmarkCount(folderRepository.countBookmarksInFolder(folder.getId()));
                    return folderResponse;
                })
                .toList();

    }

    @Override
    public PageResponse<BookmarkResponse> getBookmarksInFolder(
            Long folderId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) throws AccessDeniedException {

        User user = authUtil.loggedInUser();

        // Verify folder exists and belongs to user
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this folder");
        }

        // Sort configuration
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Fetch bookmarks in this folder
        Page<BookmarkFolder> bookmarkFolderPage = bookmarkFolderRepository.findByFolderId(folderId, pageDetails);

        // Convert to DTO
        List<BookmarkResponse> bookmarkResponses = bookmarkFolderPage
                .getContent()
                .stream()
                .map(bookmarkFolder -> {
                    Bookmark bookmark = bookmarkFolder.getBookmark();
                    BookmarkResponse response = modelMapper.map(bookmark, BookmarkResponse.class);

                    // Clear any existing folders that might have been incorrectly mapped
                    response.getFolders().clear();

                    // Fetch all folders for this bookmark from your repository
                    List<BookmarkFolder> allFoldersForBookmark = bookmarkFolderRepository
                            .findByBookmarkId(bookmark.getId());

                    // Map them to FolderMinimalResponse and add to the response
                    Set<BookmarkResponse.FolderMinimalResponse> folderSet = allFoldersForBookmark
                            .stream()
                            .map(bf -> new BookmarkResponse
                                    .FolderMinimalResponse(
                                    bf.getFolder().getId(),
                                    bf.getFolder().getName()))
                            .collect(Collectors.toSet());

                    response.setFolders(folderSet);

                    return response;
                })
                .toList();

        return new PageResponse<>(
                bookmarkResponses,
                bookmarkFolderPage.getNumber(),
                bookmarkFolderPage.getSize(),
                bookmarkFolderPage.getTotalElements(),
                bookmarkFolderPage.getTotalPages(),
                bookmarkFolderPage.isLast()
        );
    }
}
