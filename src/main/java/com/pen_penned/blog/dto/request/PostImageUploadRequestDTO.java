package com.pen_penned.blog.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageUploadRequestDTO {

    private String altText;
    private String caption;
    private Integer displayOrder;

    @JsonSetter(nulls = Nulls.SKIP)
    @Builder.Default
    private Boolean featured = false;
}
