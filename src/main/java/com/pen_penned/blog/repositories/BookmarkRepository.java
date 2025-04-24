package com.pen_penned.blog.repositories;

import com.pen_penned.blog.model.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /* // Find all bookmarks for a specific user with pagination
    Page<Bookmark> findAllByUserId(Long id, Pageable pageDetails);

    // Check if a bookmark exists for a specific user and post
    boolean existsByUserIdAndPostId(Long id, Long postId); */

    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId")
    Page<Bookmark> findBookmarksByUserId(@Param("userId") Long userId, Pageable pageDetails);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Bookmark b WHERE b.user.id = :userId AND b.post.id = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
    
}
