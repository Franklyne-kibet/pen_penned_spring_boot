package com.pen_penned.blog.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PostImageUpdateRequestDTO {

    @Size(max = 255, message = "Alt text must be less than 255 characters")
    String altText;

    String caption;

    @PositiveOrZero(message = "Display order must be a positive number or zero")
    Integer displayOrder;

    @JsonSetter(nulls = Nulls.SKIP)
    @Builder.Default
    Boolean featured = false;


    public static PostImageUpdateRequestDTO withDisplayOrder(
            PostImageUpdateRequestDTO original,
            Integer displayOrder) {
        return original.toBuilder()
                .displayOrder(displayOrder)
                .build();
    }

    public static PostImageUpdateRequestDTO createDefault(@PositiveOrZero int displayOrder) {
        return PostImageUpdateRequestDTO.builder()
                .displayOrder(displayOrder)
                .featured(false)
                .build();
    }
}
