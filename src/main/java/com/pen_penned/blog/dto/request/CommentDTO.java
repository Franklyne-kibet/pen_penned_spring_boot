package com.pen_penned.blog.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private Long commentId;
    private String content;
    private Long authorId;
    private String authorName;
    private Long postId;
    private String createdAt;
}
