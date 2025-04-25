package com.pen_penned.blog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Table(name = "folders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_folder_user_name",
                        columnNames = {"user_id", "name"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_folder_user",
                        columnList = "user_id"
                )
        }
)
public class Folder {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id", updatable = false)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    @Size(max = 255)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
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
        if (!(o instanceof Folder folder)) return false;
        return getName().equals(folder.getName()) &&
                getUser().equals(folder.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUser());
    }
}
