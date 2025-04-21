package com.pen_penned.blog.service;

import com.pen_penned.blog.dto.request.CommentDTO;
import com.pen_penned.blog.dto.response.CommentResponse;
import com.pen_penned.blog.exception.ResourceNotFoundException;
import com.pen_penned.blog.model.Comment;
import com.pen_penned.blog.model.User;
import com.pen_penned.blog.repositories.CommentRepository;
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

    private final ModelMapper modelMapper;
    private final CommentRepository commentRepository;
    private final AuthUtil authUtil;

    @Override
    public CommentDTO createComment(CommentDTO commentDTO, User user) {
        Comment comment = modelMapper.map(commentDTO, Comment.class);
        comment.setAuthor(user);

        List<Comment> commentList = user.getComments();
        commentList.add(comment);
        user.setComments(commentList);

        Comment savedComment = commentRepository.save(comment);
        return modelMapper.map(savedComment, CommentDTO.class);
    }


    @Override
    public CommentDTO getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));

        return modelMapper.map(comment, CommentDTO.class);
    }


    @Override
    public Object getCommentsByPost(
            Long postId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Comment> commentPage = commentRepository.findCommentsByPostId(postId, pageDetails);

        List<CommentDTO> commentDTOS = commentPage.getContent().stream()
                .map(comment -> modelMapper.map(comment, CommentDTO.class))
                .toList();

        return new CommentResponse(
                commentDTOS,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast()
        );
    }


    @Override
    @Transactional
    public void deleteComment(Long commentId) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId", commentId));

        User loggedInUser = authUtil.loggedInUser();
        if (!comment.getAuthor().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }

        commentRepository.delete(comment);
    }


}
