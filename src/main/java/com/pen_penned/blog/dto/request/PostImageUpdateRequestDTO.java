package com.pen_penned.blog.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageUpdateRequestDTO {

    @Size(max = 255, message = "Alt text must be less than 255 characters")
    private String altText;

    private String caption;

    @PositiveOrZero(message = "Display order must be a positive number or zero")
    private Integer displayOrder;

    @JsonSetter(nulls = Nulls.SKIP)
    @Builder.Default
    private Boolean featured = false;
}
