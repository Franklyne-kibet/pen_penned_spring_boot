package com.pen_penned.blog.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "post_images", indexes = {
        @Index(name = "idx_post_image_post", columnList = "post_id"),
        @Index(name = "idx_post_image_s3key", columnList = "s3Key")
})
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 512)
    private String s3Key;

    @Column(nullable = false, length = 512)
    private String s3Url;

    @Column(length = 512)
    private String thumbnailS3Key;

    @Column(length = 512)
    private String thumbnailS3Url;

    @Column(length = 255)
    private String altText;

    @Column(length = 512)
    private String caption;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column
    private Boolean featured;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @PrePersist
    public void prePersist() {
        if (filename == null) {
            filename = UUID.randomUUID().toString();
        }
    }

    private void sanitizeOriginalFilename() {
        if (originalFilename != null) {
            // Remove path information and only keep the base filename
            originalFilename = originalFilename.replaceAll("^.*[\\\\/]", "");
            // Optional: remove any non-ASCII or unsafe characters
            originalFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        }
    }
}
