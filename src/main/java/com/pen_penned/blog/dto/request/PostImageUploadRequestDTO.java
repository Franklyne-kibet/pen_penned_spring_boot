package com.pen_penned.blog.dto.request;

import lombok.Builder;

@Builder
public record PostImageUploadRequest (
        String altText,
        String caption,
        Integer displayOrder,
        Boolean featured
) {

}
