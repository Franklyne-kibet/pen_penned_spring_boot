package com.pen_penned.blog.repositories;

import com.pen_penned.blog.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId")
    Page<Comment> findCommentsByPostId(@Param("postId") Long postId, Pageable pageDetails);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    int getCommentCountByPostId(@Param("postId") Long postId);
}
