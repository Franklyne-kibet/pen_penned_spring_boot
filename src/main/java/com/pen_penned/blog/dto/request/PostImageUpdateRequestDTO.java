package com.pen_penned.blog.dto.request;

public record PostImageUpdateRequest(
        String altText,
        String caption,
        Integer displayOrder,
        Boolean featured
) {

}
