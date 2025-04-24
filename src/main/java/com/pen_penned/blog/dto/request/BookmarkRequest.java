package com.pen_penned.blog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookmarkRequestDTO {

    @NotNull(message = "Post ID is required")
    private Long postId;
}
