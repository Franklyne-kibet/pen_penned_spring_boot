package com.pen_penned.blog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "bookmarks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookmark_user_post",
                        columnNames = {"user_id", "post_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_bookmark_user_post",
                        columnList = "user_id, post_id",
                        unique = true
                )
        }
)

public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id", updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookmarkFolder> bookmarkFolders = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bookmark bookmark)) return false;
        return getUser().equals(bookmark.getUser()) &&
                getPost().equals(bookmark.getPost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getPost());
    }
}
