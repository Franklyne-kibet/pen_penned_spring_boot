package com.pen_penned.blog.service;

import com.pen_penned.blog.exceptions.ResourceNotFoundException;
import com.pen_penned.blog.model.Post;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.payload.PostDTO;
import com.pen_penned.blog.payload.PostResponse;
import com.pen_penned.blog.repositories.PostRepository;
import com.pen_penned.blog.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    private final AuthUtil authUtil;

    @Override
    public PostDTO createPost(PostDTO postDTO, User user) {
        Post post = modelMapper.map(postDTO, Post.class);
        post.setAuthor(user);

        List<Post> postList = user.getPosts();
        postList.add(post);
        user.setPosts(postList);

        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, PostDTO.class);
    }

    @Override
    public PostResponse getAllPosts(Integer pageNumber, Integer pageSize,
                                    String sortBy, String sortOrder) {

        Sort sortByAnyOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAnyOrder);

        Page<Post> pagePosts = postRepository.findAll(pageDetails);

        List<Post> posts = pagePosts.getContent();

        List<PostDTO> postDTOS = posts.stream()
                .map(post -> modelMapper.map(post, PostDTO.class))
                .toList();

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(postDTOS);
        postResponse.setPageNumber(pagePosts.getNumber());
        postResponse.setPageSize(pagePosts.getSize());
        postResponse.setTotalElements(pagePosts.getTotalElements());
        postResponse.setTotalPages(pagePosts.getTotalPages());
        postResponse.setLastPage(pagePosts.isLast());
        return postResponse;
    }

    @Override
    public PostDTO getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        return modelMapper.map(post, PostDTO.class);
    }

    @Override
    public List<PostDTO> getUserPosts(User user) {
        List<Post> posts = postRepository.findByAuthor(user);
        return posts.stream()
                .map(post -> modelMapper.map(post, PostDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public PostDTO updatePost(Long postId, PostDTO postDTO) throws AccessDeniedException {
        // Fetch the existing post
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        // Check if the logged-in user is the owner of the post
        User loggedInUser = authUtil.loggedInUser();
        if (!existingPost.getAuthor().getUserId().equals(loggedInUser.getUserId())) {
            throw new AccessDeniedException("You do not have permission to update this post.");
        }

        // Update only non-null fields
        if (postDTO.getTitle() != null) existingPost.setTitle(postDTO.getTitle());
        if (postDTO.getContent() != null) existingPost.setContent(postDTO.getContent());
        if (postDTO.getSlug() != null) existingPost.setSlug(postDTO.getSlug());
        if (postDTO.getTags() != null) existingPost.setTags(postDTO.getTags());
        if (postDTO.getCoverImageUrl() != null) existingPost.setCoverImageUrl(postDTO.getCoverImageUrl());
        if (postDTO.getIsPublished() != null) existingPost.setPublished(postDTO.getIsPublished());

        // Save updated post
        Post updatedPost = postRepository.save(existingPost);
        return modelMapper.map(updatedPost, PostDTO.class);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) throws AccessDeniedException {
        // Fetch the post from the database
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

        User loggedInUser = authUtil.loggedInUser();
        if (!post.getAuthor().getUserId().equals(loggedInUser.getUserId())) {
            throw new AccessDeniedException("You do not have permission to delete this post.");
        }

        // Delete the post from the repository
        postRepository.delete(post);
    }
}
