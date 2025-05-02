package com.pen_penned.blog.controller;

import com.pen_penned.blog.dto.request.PostImageUpdateRequestDTO;
import com.pen_penned.blog.dto.request.PostImageUploadRequestDTO;
import com.pen_penned.blog.dto.response.PostImageResponseDTO;
import com.pen_penned.blog.exception.ImageProcessingException;
import com.pen_penned.blog.model.PostImage;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.service.PostImageService;
import com.pen_penned.blog.service.PostService;
import com.pen_penned.blog.util.AuthUtil;
import com.pen_penned.blog.util.ImageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/images")
public class PostImageController {

    private final PostImageService postImageService;
    private final PostService postService;
    private final AuthUtil authUtil;

    // Single image upload endpoint
    @PostMapping(value = "/single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostImageResponseDTO> uploadSingleImage(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file,
            @RequestPart(value = "metadata", required = false)
            PostImageUploadRequestDTO metadata) throws IOException {

        User user = authUtil.loggedInUser();

        // Verify user has permission to modify this post
        postService.verifyPostOwnership(postId, user.getId());

        if (file == null || file.isEmpty()) {
            throw ImageProcessingException.invalidFormat();
        }

        // Validate content type
        ImageValidator.validateImage(file);

        try {
            PostImage uploadedImage = postImageService
                    .uploadPostImage(postId, file, metadata)
                    .get(); // Wait for async processing to complete

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(PostImageResponseDTO.fromEntity(uploadedImage));
        } catch (InterruptedException | ExecutionException e) {
            throw new ImageProcessingException("Image upload failed", "UPLOAD_FAILED", e);
        }
    }


    // Multiple images upload endpoint (moved from PostController)
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PostImageResponseDTO>> uploadMultipleImages(
            @PathVariable Long postId,
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart(value = "imageMetadata", required = false)
            List<PostImageUploadRequestDTO> imageMetadataList) throws AccessDeniedException {

        User user = authUtil.loggedInUser();

        // Verify user has permission to modify this post
        postService.verifyPostOwnership(postId, user.getId());

        List<CompletableFuture<PostImage>> futures = new ArrayList<>();

        // Prepare metadata list if needed
        if (imageMetadataList == null) {
            imageMetadataList = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                imageMetadataList.add(new PostImageUploadRequestDTO());
            }
        } else if (imageMetadataList.size() < images.size()) {
            for (int i = imageMetadataList.size(); i < images.size(); i++) {
                imageMetadataList.add(new PostImageUploadRequestDTO());
            }
        }

        // Upload each image
        for (int i = 0; i < images.size(); i++) {
            PostImageUploadRequestDTO metadata = imageMetadataList.get(i);
            metadata.setDisplayOrder(metadata.getDisplayOrder() != null ? metadata.getDisplayOrder() : i);
            futures.add(postImageService.uploadPostImage(postId, images.get(i), metadata));
        }

        try {
            // Wait for all uploads to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            // Return the list of images
            return new ResponseEntity<>(postImageService.getPostImageDTOs(postId).stream().toList(),
                    HttpStatus.CREATED);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(postImageService.getPostImageDTOs(postId).stream().toList());
        }
    }

    @GetMapping
    public ResponseEntity<Set<PostImageResponseDTO>> getPostImages(@PathVariable Long postId) {
        Set<PostImageResponseDTO> images = postImageService.getPostImages(postId).stream()
                .map(PostImageResponseDTO::fromEntity)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<PostImageResponseDTO> getImage(
            @PathVariable Long postId,
            @PathVariable Long imageId) {

        return postImageService.getImageById(imageId)
                .map(PostImageResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ImageProcessingException("Image not found", "NOT_FOUND"));
    }

    @GetMapping("/{imageId}/presigned")
    public ResponseEntity<String> getPresignedUrl(
            @PathVariable Long postId,
            @PathVariable Long imageId,
            @RequestParam(defaultValue = "10") long expirationMinutes) {

        String presignedUrl = postImageService.getPresignedUrl(imageId, expirationMinutes);
        return ResponseEntity.ok(presignedUrl);
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<PostImageResponseDTO> updateImageMetadata(
            @PathVariable Long postId,
            @PathVariable Long imageId,
            @RequestBody PostImageUpdateRequestDTO updateRequest
    ) {
        PostImage updated = postImageService.updateImageMetadata(imageId, updateRequest);
        return ResponseEntity.ok(PostImageResponseDTO.fromEntity(updated));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long postId,
            @PathVariable Long imageId) {

        postImageService.deletePostImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
