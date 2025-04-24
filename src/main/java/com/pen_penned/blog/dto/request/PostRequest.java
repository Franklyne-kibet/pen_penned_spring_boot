package com.pen_penned.blog.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDTO {

    private String title;
    private String content;
    private String slug;
    private List<String> tags;
    private String coverImageUrl;
    private Boolean published = false;
}
