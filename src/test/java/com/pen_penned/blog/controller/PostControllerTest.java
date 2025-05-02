/*
package com.pen_penned.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pen_penned.blog.dto.request.PostImageUploadRequestDTO;
import com.pen_penned.blog.dto.request.PostRequest;
import com.pen_penned.blog.dto.response.PostImageResponseDTO;
import com.pen_penned.blog.dto.response.PostResponse;
import com.pen_penned.blog.model.Post;
import com.pen_penned.blog.model.PostImage;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.service.PostImageService;
import com.pen_penned.blog.service.PostService;
import com.pen_penned.blog.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private PostImageService postImageService;

    @MockitoBean
    private AuthUtil authUtil;

    private PostResponse testPostResponse;
    private PostRequest testPostRequest;
    private PostImage testPostImage;
    private PostImageResponseDTO testImageResponseDTO;

    @BeforeEach
    public void setup() {
        // Set up test user
        User testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Set up test post
        Post testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setAuthor(testUser);

        // Set up test post request
        testPostRequest = new PostRequest();
        testPostRequest.setTitle("Test Post");
        testPostRequest.setContent("Test Content");

        // Set up test post response
        testPostResponse = new PostResponse();
        testPostResponse.setId(1L);
        testPostResponse.setTitle("Test Post");
        testPostResponse.setContent("Test Content");
        testPostResponse.setAuthorId(1L);
        testPostResponse.setAuthorFirstName("Test");
        testPostResponse.setAuthorLastName("User");
        testPostResponse.setCommentCount(0);

        // Set up test post image
        testPostImage = new PostImage();
        testPostImage.setId(1L);
        testPostImage.setS3Url("https://example.com/image.jpg");
        testPostImage.setThumbnailS3Url("https://example.com/thumbnail.jpg");
        testPostImage.setOriginalFilename("image.jpg");
        testPostImage.setPost(testPost);

        // Set up test image response
        testImageResponseDTO = new PostImageResponseDTO();
        testImageResponseDTO.setId(1L);
        testImageResponseDTO.setImageUrl("https://example.com/image.jpg");
        testImageResponseDTO.setThumbnailUrl("https://example.com/thumbnail.jpg");
        testImageResponseDTO.setOriginalFilename("image.jpg");

        // Mock AuthUtil
        when(authUtil.loggedInUser()).thenReturn(testUser);
    }

    @Test
    public void testCreatePostWithImagesSuccess() throws Exception {
        // Mock PostService.createPost
        when(postService.createPost(any(PostRequest.class), any(User.class)))
                .thenReturn(testPostResponse);

        // Mock PostService.getPostResponseById
        when(postService.getPostResponseById(anyLong()))
                .thenReturn(testPostResponse);

        // Mock PostImageService.uploadPostImage
        when(postImageService.uploadPostImage(anyLong(), any(), any(PostImageUploadRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(testPostImage));

        // Create test files
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        MockMultipartFile postData = new MockMultipartFile(
                "post",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(testPostRequest)
        );

        PostImageUploadRequestDTO imageMetadata = new PostImageUploadRequestDTO();
        imageMetadata.setAltText("Test Alt Text");
        imageMetadata.setCaption("Test Caption");

        MockMultipartFile metadataFile = new MockMultipartFile(
                "imageMetadata",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(List.of(imageMetadata))
        );

        // Perform the request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/posts")
                        .file(postData)
                        .file(image)
                        .file(metadataFile))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Post"));
    }

    @Test
    public void testAddImagesToPostSuccess() throws Exception {
        // Mock PostService.verifyPostOwnership - do nothing means no exception
        doNothing().when(postService).verifyPostOwnership(anyLong(), anyLong());

        // Mock PostImageService.uploadPostImage
        when(postImageService.uploadPostImage(anyLong(), any(), any(PostImageUploadRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(testPostImage));

        // Mock PostImageService.getPostImageDTOs
        HashSet<PostImageResponseDTO> responseSet = new HashSet<>();
        responseSet.add(testImageResponseDTO);
        when(postImageService.getPostImageDTOs(anyLong())).thenReturn(responseSet);

        // Create test files
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        PostImageUploadRequestDTO imageMetadata = new PostImageUploadRequestDTO();
        imageMetadata.setAltText("Test Alt Text");
        imageMetadata.setCaption("Test Caption");

        MockMultipartFile metadataFile = new MockMultipartFile(
                "imageMetadata",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(List.of(imageMetadata))
        );

        // Perform the request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/posts/1/images")
                        .file(image)
                        .file(metadataFile))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].url").value("https://example.com/image.jpg"));
    }

    @Test
    public void testDeletePostImageSuccess() throws Exception {
        // Mock PostService.verifyPostOwnership - do nothing means no exception
        doNothing().when(postService).verifyPostOwnership(anyLong(), anyLong());

        // Mock PostImageService.deletePostImage
        doNothing().when(postImageService).deletePostImage(anyLong());

        // Perform the request
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/posts/1/images/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testCreatePostWithoutImagesSuccess() throws Exception {
        // Mock PostService.createPost
        when(postService.createPost(any(PostRequest.class), any(User.class)))
                .thenReturn(testPostResponse);

        // Perform the request (JSON request for posts without images)
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(testPostRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Post"));
    }

}
*/
