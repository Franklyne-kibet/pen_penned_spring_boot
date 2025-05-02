package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.PostImageUpdateRequestDTO;
import com.pen_penned.blog.dto.request.PostImageUploadRequestDTO;
import com.pen_penned.blog.dto.response.PostImageResponseDTO;
import com.pen_penned.blog.exception.ImageProcessingException;
import com.pen_penned.blog.exception.ResourceNotFoundException;
import com.pen_penned.blog.model.Post;
import com.pen_penned.blog.model.PostImage;
import com.pen_penned.blog.repositories.PostImageRepository;
import com.pen_penned.blog.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostImageServiceImpl implements PostImageService {

    private static final String POST_IMAGES_DIRECTORY = "cdp-events-test";
    private static final String THUMBNAILS_DIRECTORY = "cdp-events-test";

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final S3Service s3Service;
    private final ImageProcessingService imageProcessingService;
    
    @Override
    public CompletableFuture<PostImage> uploadPostImage(Long postId, MultipartFile file,
                                                        PostImageUploadRequestDTO metadata) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        try {
            // Upload original image
            String s3Key = s3Service.uploadFile(POST_IMAGES_DIRECTORY, file);
            String s3Url = s3Service.getFileUrl(s3Key);

            return imageProcessingService.createThumbnail(file)
                    .thenCompose(thumbnailStream -> {
                        try {
                            byte[] thumbnailBytes = thumbnailStream.readAllBytes();
                            ByteArrayInputStream uploadStream = new ByteArrayInputStream(thumbnailBytes);
                            int thumbnailSize = thumbnailBytes.length;
                            String thumbnailFilename = "thumb_" + file.getOriginalFilename();
                            String thumbnailS3Key = s3Service.uploadStream(
                                    THUMBNAILS_DIRECTORY,
                                    thumbnailFilename,
                                    file.getContentType(),
                                    uploadStream,
                                    thumbnailSize);
                            String thumbnailS3Url = s3Service.getFileUrl(thumbnailS3Key);

                            PostImage.PostImageBuilder builder = PostImage.builder()
                                    .filename(file.getName())
                                    .originalFilename(file.getOriginalFilename())
                                    .contentType(file.getContentType())
                                    .fileSize(file.getSize())
                                    .s3Key(s3Key)
                                    .s3Url(s3Url)
                                    .thumbnailS3Key(thumbnailS3Key)
                                    .thumbnailS3Url(thumbnailS3Url)
                                    .post(post);

                            Optional.ofNullable(metadata).ifPresent(meta -> {
                                builder.altText(meta.getAltText())
                                        .caption(meta.getCaption())
                                        .displayOrder(meta.getDisplayOrder())
                                        .featured(meta.getFeatured());
                            });

                            PostImage postImage = builder.build();
                            post.addImage(postImage);
                            return CompletableFuture.completedFuture(postImageRepository.save(postImage));

                        } catch (Exception e) {
                            log.error("Failed to process thumbnail", e);
                            throw new CompletionException("Failed to upload thumbnail", e);
                        }
                    });

        } catch (Exception e) {
            log.error("Failed to upload image", e);
            throw ImageProcessingException.uploadFailed(e);
        }
    }

    @Override
    @Transactional
    public PostImage updateImageMetadata(Long imageId, PostImageUpdateRequestDTO updateRequest) {
        PostImage image = postImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("PostImage", "id", imageId));

        Optional.ofNullable(updateRequest.getAltText()).ifPresent(image::setAltText);
        Optional.ofNullable(updateRequest.getCaption()).ifPresent(image::setCaption);
        Optional.ofNullable(updateRequest.getDisplayOrder()).ifPresent(image::setDisplayOrder);
        Optional.ofNullable(updateRequest.getFeatured()).ifPresent(image::setFeatured);

        return postImageRepository.save(image);
    }

    @Override
    @Transactional
    public void deletePostImage(Long imageId) {
        PostImage image = postImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("PostImage", "id", imageId));

        try {
            log.info("Deleting image ID {} with S3 key {}", imageId, image.getS3Key());
            s3Service.deleteFile(image.getS3Key());
            Optional.ofNullable(image.getThumbnailS3Key()).ifPresent(s3Service::deleteFile);

            Post post = image.getPost();
            if (post != null) {
                post.removeImage(image);
            }

            postImageRepository.delete(image);
        } catch (Exception e) {
            log.error("Failed to delete image with ID: {}", imageId, e);
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PostImage> getPostImages(Long postId) {
        return new HashSet<>(postImageRepository.findByPostId(postId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostImage> getImageById(Long imageId) {
        return postImageRepository.findById(imageId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PostImageResponseDTO> getPostImageDTOs(Long postId) {
        return getPostImages(postId).stream()
                .map(PostImageResponseDTO::fromEntity)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    @Transactional(readOnly = true)
    public String getPresignedUrl(Long imageId, long expirationMinutes) {
        PostImage image = postImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("PostImage", "id", imageId));

        return s3Service.generatePresignedUrl(image.getS3Key(), expirationMinutes);
    }
}
