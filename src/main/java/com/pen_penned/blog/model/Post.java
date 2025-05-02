package com.pen_penned.blog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "posts", indexes = {
        @Index(name = "idx_slug", columnList = "slug", unique = true),
        @Index(name = "idx_published", columnList = "is_published"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_author", columnList = "user_id")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", updatable = false)
    private Long id;

    @NotBlank
    @Size(min = 5, message = "Title must be between 5 and 200 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Content cannot be empty")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "is_published", nullable = false)
    private Boolean published;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "cover_image_url", length = 512)
    @URL(message = "Cover image URL must be valid")
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private long version;


    @PrePersist
    @PreUpdate
    public void validateSlug() {
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = generateSlugFromTitle();
        }
        this.slug = this.slug.trim().toLowerCase().replaceAll("[^a-z0-9-]", "-");
    }

    private String generateSlugFromTitle() {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        return baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // Helper method for adding comments
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    // Helper method for removing comments
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    // Helper method for image management
    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this);
    }

    public void removeImage(PostImage image) {
        images.remove(image);
        image.setPost(null);
    }
}
