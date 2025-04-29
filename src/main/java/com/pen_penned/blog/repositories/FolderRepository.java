package com.pen_penned.blog.repositories;

import com.pen_penned.blog.model.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    Page<Folder> findByUserId(Long userId, Pageable pageDetails);

    Optional<Folder> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

    @Query("SELECT f FROM Folder f JOIN FETCH f.bookmarkFolders bf JOIN FETCH bf.bookmark b WHERE b.id = :bookmarkId")
    List<Folder> findByBookmarkId(@Param("bookmarkId") Long bookmarkId);

    @Query("SELECT COUNT(bf) FROM BookmarkFolder bf WHERE bf.folder.id = :folderId")
    Integer countBookmarksInFolder(@Param("folderId") Long folderId);
}
