package com.pen_penned.blog.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderResponse {

    private Long id;
    private String name;
    private String description;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer bookmarkCount;
}
