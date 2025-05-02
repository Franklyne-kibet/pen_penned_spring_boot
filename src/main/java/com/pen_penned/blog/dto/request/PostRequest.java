package com.pen_penned.blog.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    private String title;
    private String content;
    private String slug;
    private List<String> tags;
    private String coverImageUrl;

    @Builder.Default
    private List<Long> imageIds = new ArrayList<>();

    @Builder.Default
    private Boolean published = false;
}
