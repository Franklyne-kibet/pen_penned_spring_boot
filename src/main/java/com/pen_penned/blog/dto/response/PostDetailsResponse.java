package com.pen_penned.blog.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pen_penned.blog.dto.request.CommentRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailsResponse {

    private Long id;
    private String title;
    private String content;
    private String slug;
    private List<String> tags;
    private String coverImageUrl;

    private Boolean published = false;

    private Long authorId;
    private String authorFirstName;
    private String authorLastName;

    private Integer commentCount;
    private List<CommentRequest> comments;

    @Builder.Default
    private List<PostImageResponseDTO> images = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
