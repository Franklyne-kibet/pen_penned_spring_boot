package com.pen_penned.blog.controller;

import com.pen_penned.blog.config.AppConstants;
import com.pen_penned.blog.dto.request.PostImageUploadRequestDTO;
import com.pen_penned.blog.dto.request.PostRequest;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.dto.response.PostDetailsResponse;
import com.pen_penned.blog.dto.response.PostResponse;
import com.pen_penned.blog.model.PostImage;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.service.PostImageService;
import com.pen_penned.blog.service.PostService;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final AuthUtil authUtil;
    private final PostService postService;
    private final PostImageService postImageService;

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPostWithImages(
            @Valid @RequestPart("post") PostRequest postRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "imageMetadata", required = false)
            List<PostImageUploadRequestDTO> imageMetadataList
    ) {
        User user = authUtil.loggedInUser();

        // Fist create the post
        PostResponse savedPost = postService.createPost(postRequest, user);

        // If images are provided, upload them
        if (images != null && !images.isEmpty()) {
            List<CompletableFuture<PostImage>> futures = new ArrayList<>();
            List<PostImageUploadRequestDTO> processedMetadata = new ArrayList<>();

            // Prepare metadata with proper display orders
            if (imageMetadataList == null || imageMetadataList.isEmpty()) {
                // Create default metadata for each image
                for (int i = 0; i < images.size(); i++) {
                    processedMetadata.add(PostImageUploadRequestDTO.createDefault(i));
                }
            } else {
                // Process existing metadata
                for (int i = 0; i < images.size(); i++) {
                    if (i < imageMetadataList.size()) {
                        PostImageUploadRequestDTO original = imageMetadataList.get(i);
                        Integer displayOrder = original.getDisplayOrder() != null ? original.getDisplayOrder() : i;
                        processedMetadata.add(PostImageUploadRequestDTO.withDisplayOrder(original, displayOrder));
                    } else {
                        // For any extra images without metadata
                        processedMetadata.add(PostImageUploadRequestDTO.createDefault(i));
                    }
                }
            }

            // Upload each image asynchronously
            for (int i = 0; i < images.size(); i++) {
                futures.add(postImageService.uploadPostImage(savedPost.getId(), images.get(i),
                        processedMetadata.get(i)));
            }

            try {
                // Wait for all uploads to complete
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

                // Retrieve the updated post with images
                savedPost = postService.getPostResponseById(savedPost.getId());
            } catch (InterruptedException | ExecutionException ignored) {
                // Continue with the post creation even if some images fail to upload
                // The successfully uploaded images will still be associated with the post
            }
        }

        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest) {
        User user = authUtil.loggedInUser();
        PostResponse savedPost = postService.createPost(postRequest, user);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> getAllPosts(
            @RequestParam(name = "pageNumber",
                    defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",
                    defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy",
                    defaultValue = AppConstants.SORT_POSTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder",
                    defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        PageResponse<PostResponse> postResponse = postService.getAllPosts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailsResponse> getPostById(@PathVariable Long postId) {
        PostDetailsResponse postDetailsResponse = postService.getPostById(postId);
        return new ResponseEntity<>(postDetailsResponse, HttpStatus.OK);
    }


    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePostById(
            @PathVariable Long postId, @Valid @RequestBody PostRequest postRequest) throws AccessDeniedException {
        PostResponse updatedPost = postService.updatePost(postId, postRequest);
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) throws AccessDeniedException {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}
