package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.PostImageUpdateRequestDTO;
import com.pen_penned.blog.dto.request.PostImageUploadRequestDTO;
import com.pen_penned.blog.dto.response.PostImageResponseDTO;
import com.pen_penned.blog.model.PostImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface PostImageService {

    /**
     * Upload an image with metadata and associate it with a post
     *
     * @param postId   ID of the post
     * @param file     Image file to upload
     * @param metadata Additional metadata for the image
     * @return The created PostImage entity
     */
    CompletableFuture<PostImage> uploadPostImage(Long postId, MultipartFile file,
                                                 PostImageUploadRequestDTO metadata);

    /**
     * Update image metadata
     *
     * @param imageId       ID of the image to update
     * @param updateRequest New metadata values
     * @return The updated PostImage entity
     */
    PostImage updateImageMetadata(Long imageId, PostImageUpdateRequestDTO updateRequest);


    /**
     * Delete an image by ID
     *
     * @param imageId ID of the image to delete
     */
    void deletePostImage(Long imageId);

    /**
     * Get all images for a post
     *
     * @param postId ID of the post
     * @return Set of post images
     */
    Set<PostImage> getPostImages(Long postId);

    /**
     * Get an image by ID
     *
     * @param imageId ID of the image
     * @return Optional containing the image if found
     */
    Optional<PostImage> getImageById(Long imageId);

    /**
     * Get all images for a post as DTOs
     *
     * @param postId ID of the post
     * @return Set of image DTOs
     */
    Set<PostImageResponseDTO> getPostImageDTOs(Long postId);

    /**
     * Get a presigned URL for an image
     *
     * @param imageId           ID of the image
     * @param expirationMinutes Minutes before URL expires
     * @return Presigned URL for temporary access
     */
    String getPresignedUrl(Long imageId, long expirationMinutes);
}
