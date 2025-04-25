package com.pen_penned.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponse {

    private Long id;
    private Long userId;
    private Long postId;
    private String postTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Set<FolderMinimalResponse> folders = new HashSet<>();


    // Inner class for minimal folder information to avoid circular references
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FolderMinimalResponse {

        private Long id;
        private String name;
    }

}
