package com.pen_penned.blog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record BookmarkFolderRequest(

        @NotNull(message = "Bookmark ID cannot be null")
        Long bookmarkId,

        @NotNull(message = "Folder IDs cannot be null")
        Set<Long> folderIds
) {

}
