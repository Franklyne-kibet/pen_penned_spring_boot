package com.pen_penned.blog.controller;


import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.dto.request.BookmarkFolderRequest;
import com.pen_penned.blog.dto.request.FolderRequest;
import com.pen_penned.blog.dto.response.BookmarkResponse;
import com.pen_penned.blog.dto.response.FolderResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.service.FolderService;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class FolderController {

    private final AuthUtil authUtil;
    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody FolderRequest folderRequest) {
        authUtil.loggedInUser(); // Ensure user is authenticated
        FolderResponse folderResponse = folderService.createFolder(folderRequest);
        return new ResponseEntity<>(folderResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<FolderResponse>> getUserFolders(
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = "name", required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        authUtil.loggedInUser(); // Ensure user is authenticated
        PageResponse<FolderResponse> folders = folderService.getUserFolders(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(folders, HttpStatus.OK);
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse> getFolder(@PathVariable Long folderId) throws AccessDeniedException {
        authUtil.loggedInUser(); // Ensure user is authenticated
        FolderResponse folderResponse = folderService.getFolder(folderId);
        return new ResponseEntity<>(folderResponse, HttpStatus.OK);
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<FolderResponse> updateFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody FolderRequest folderRequest
    ) throws AccessDeniedException {
        authUtil.loggedInUser(); // Ensure user is authenticated
        FolderResponse folderResponse = folderService.updateFolder(folderId, folderRequest);
        return new ResponseEntity<>(folderResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) throws AccessDeniedException {
        authUtil.loggedInUser(); // Ensure user is authenticated
        folderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bookmarks")
    public ResponseEntity<Void> addBookmarkToFolders(@Valid @RequestBody BookmarkFolderRequest bookmarkFolderRequest)
            throws AccessDeniedException {
        authUtil.loggedInUser(); // Ensure user is authenticated
        folderService.addBookmarkToFolders(bookmarkFolderRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bookmarks/{bookmarkId}/folders/{folderId}")
    public ResponseEntity<Void> removeBookmarkFromFolder(
            @PathVariable Long bookmarkId,
            @PathVariable Long folderId
    ) throws AccessDeniedException {
        authUtil.loggedInUser(); // Ensure user is authenticated
        folderService.removeBookmarkFromFolder(bookmarkId, folderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<List<FolderResponse>> getBookmarkFolders(@PathVariable Long bookmarkId) {
        authUtil.loggedInUser(); // Ensure user is authenticated
        List<FolderResponse> folders = folderService.getBookmarkFolders(bookmarkId);
        return new ResponseEntity<>(folders, HttpStatus.OK);
    }

    @GetMapping("/{folderId}/bookmarks")
    public ResponseEntity<PageResponse<BookmarkResponse>> getBookmarksInFolder(
            @PathVariable Long folderId,
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_COMMENTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) throws AccessDeniedException {
        authUtil.loggedInUser(); // Ensure user is authenticated
        PageResponse<BookmarkResponse> bookmarks = folderService.getBookmarksInFolder(folderId, pageNumber,
                pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(bookmarks, HttpStatus.OK);
    }
}
