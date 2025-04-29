package com.pen_penned.blog.repositories;

import com.pen_penned.blog.model.BookmarkFolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkFolderRepository extends JpaRepository<BookmarkFolder, Long> {

    List<BookmarkFolder> findByBookmarkId(Long bookmarkId);

    void deleteByBookmarkIdAndFolderId(Long bookmarkId, Long folderId);

    Optional<BookmarkFolder> findByBookmarkIdAndFolderId(Long bookmarkId, Long folderId);

    Page<BookmarkFolder> findByFolderId(Long folderId, Pageable pageable);

    @Query("SELECT bf FROM BookmarkFolder bf JOIN FETCH bf.folder WHERE bf.bookmark.id = :bookmarkId")
    List<BookmarkFolder> findByBookmarkIdWithFolder(@Param("bookmarkId") Long bookmarkId);
}
