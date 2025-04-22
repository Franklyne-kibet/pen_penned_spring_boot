package com.pen_penned.blog.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailsDTO {

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
    private List<CommentDTO> comments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
