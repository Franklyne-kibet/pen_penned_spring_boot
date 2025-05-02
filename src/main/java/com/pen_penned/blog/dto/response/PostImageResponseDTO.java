package com.pen_penned.blog.dto.response;

import com.pen_penned.blog.model.PostImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageResponseDTO {

    private Long id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String imageUrl;
    private String thumbnailUrl;
    private String altText;
    private String caption;
    private Integer displayOrder;
    private Boolean featured;
    private LocalDateTime uploadedAt;

    public static PostImageResponseDTO fromEntity(PostImage image) {
        return PostImageResponseDTO.builder()
                .id(image.getId())
                .originalFilename(image.getOriginalFilename())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .imageUrl(image.getS3Url())
                .thumbnailUrl(image.getThumbnailS3Url())
                .uploadedAt(image.getUploadedAt())
                .altText(image.getAltText())
                .caption(image.getCaption())
                .displayOrder(image.getDisplayOrder())
                .featured(image.getFeatured())
                .build();
    }
}
