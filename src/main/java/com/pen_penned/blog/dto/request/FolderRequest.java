package com.pen_penned.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record FolderRequest(
        @NotBlank(message = "Folder name cannot be blank")
        @Size(min = 3, max = 50, message = "Folder name must be between 3 and 50 characters")
        String name,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description
) {

}
