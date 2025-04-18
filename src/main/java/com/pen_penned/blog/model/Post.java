package com.pen_penned.blog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts", indexes = {
        @Index(name = "idx_slug", columnList = "slug"),
        @Index(name = "idx_published", columnList = "isPublished"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @NotBlank
    @Size(min = 5, message = "Title must be at least 5 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Content cannot be empty")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private Boolean isPublished;

    @Column
    private String tags;

    @Column
    @URL
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @PrePersist
    public void prePersist() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = generateUniqueSlug();
        }
    }

    private String generateUniqueSlug() {
        return title.toLowerCase().replace(" ", "-") + "-" + (int) (Math.random() * 10000);
    }
}