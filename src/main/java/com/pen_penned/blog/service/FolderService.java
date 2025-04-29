package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.BookmarkFolderRequest;
import com.pen_penned.blog.dto.request.FolderRequest;
import com.pen_penned.blog.dto.response.BookmarkResponse;
import com.pen_penned.blog.dto.response.FolderResponse;
import com.pen_penned.blog.dto.response.PageResponse;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface FolderService {


    FolderResponse createFolder(FolderRequest folderRequest);

    FolderResponse getFolder(Long folderId) throws AccessDeniedException;

    FolderResponse updateFolder(Long folderId, FolderRequest folderRequest) throws AccessDeniedException;

    void deleteFolder(Long folderId) throws AccessDeniedException;

    PageResponse<FolderResponse> getUserFolders(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder);

    void addBookmarkToFolders(BookmarkFolderRequest bookmarkFolderRequest) throws AccessDeniedException;

    void removeBookmarkFromFolder(Long bookmarkId, Long folderId) throws AccessDeniedException;

    List<FolderResponse> getBookmarkFolders(Long bookmarkId);

    PageResponse<BookmarkResponse> getBookmarksInFolder(
            Long folderId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) throws AccessDeniedException;
}
