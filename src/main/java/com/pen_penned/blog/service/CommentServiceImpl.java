package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.CommentRequest;
import com.pen_penned.blog.dto.response.CommentResponse;
import com.pen_penned.blog.dto.response.PageResponse;
import com.pen_penned.blog.exception.ResourceNotFoundException;
import com.pen_penned.blog.model.Comment;
import com.pen_penned.blog.model.Post;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.CommentRepository;
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
public class CommentServiceImpl implements CommentService {

    private final AuthUtil authUtil;
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentResponse createComment(CommentRequest commentRequest, User user) {
        Comment comment = Comment.builder()
                .content(commentRequest.getContent())
                .author(user)
                .build();

        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "postId", commentRequest.getPostId()));
        comment.setPost(post);

        post.addComment(comment);
        user.addComment(comment);

        // save the comment in the database
        Comment savedComment = commentRepository.save(comment);

        CommentResponse commentResponse = modelMapper.map(savedComment, CommentResponse.class);

        // Set author details
        commentResponse.setAuthorFirstName(user.getFirstName());
        commentResponse.setAuthorLastName(user.getLastName());
        commentResponse.setPostId(commentRequest.getPostId());

        return commentResponse;
    }


    @Override
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));

        return modelMapper.map(comment, CommentResponse.class);
    }


    @Override
    public PageResponse<CommentResponse> getCommentsByPost(
            Long postId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) {

        //  Sort configuration
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Fetch comments
        Page<Comment> commentPage = commentRepository.findCommentsByPostId(postId, pageDetails);

        // Convert comments to DTOs
        List<CommentResponse> commentResponse = commentPage.getContent().stream()
                .map(comment -> modelMapper.map(comment, CommentResponse.class))
                .toList();

        // Return paginated response
        return new PageResponse<>(
                commentResponse,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast()
        );
    }

    @Transactional
    @Override
    public CommentResponse updateComment(
            Long commentId,
            CommentRequest commentRequest) throws AccessDeniedException {
        // Fetch the existing comment
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));

        // Check if the logged-in user is the owner of the comment
        User loggedInUser = authUtil.loggedInUser();
        if (!existingComment.getAuthor().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("You do not have permission to update this comment.");
        }

        // Update only non-null fields
        if (commentRequest.getContent() != null)
            existingComment.setContent(commentRequest.getContent());

        // Save the updated comment
        Comment updatedComment = commentRepository.save(existingComment);

        // Map to response
        return modelMapper.map(updatedComment, CommentResponse.class);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) throws AccessDeniedException {
        // Fetch the comment from the database
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));

        // Get the logged-in user
        User loggedInUser = authUtil.loggedInUser();

        // Check if the logged-in user is the author of the comment
        if (!comment.getAuthor().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }

        comment.getPost().removeComment(comment);
        comment.getAuthor().removeComment(comment);

        // Delete the comment from the repository
        commentRepository.delete(comment);
    }

}
