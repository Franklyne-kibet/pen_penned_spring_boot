package com.pen_penned.blog.repositories;

import com.pen_penned.blog.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostId(Long postId);

    Optional<PostImage> findByS3Key(String s3Key);

    void deleteByS3Key(String s3Key);
}
