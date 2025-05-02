package com.pen_penned.blog.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "bookmark_folders", indexes = {
        @Index(name = "idx_bookmark_folder_bookmark", columnList = "bookmark_id"),
        @Index(name = "idx_bookmark_folder_folder", columnList = "folder_id")
})
public class BookmarkFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_folder_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bookmark_id", nullable = false)
    private Bookmark bookmark;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookmarkFolder that)) return false;
        return getBookmark().equals(that.getBookmark()) &&
                getFolder().equals(that.getFolder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBookmark(), getFolder());
    }
}
