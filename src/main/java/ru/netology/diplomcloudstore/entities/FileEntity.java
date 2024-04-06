package ru.netology.diplomcloudstore.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//Оставил для примера. Отключил в конфигурации. Все создается через liquibase
@Table(
        name = "files_cloudstore",
        indexes = {
                @Index(
                        name = "composit_file_index_1",
                        columnList = "file_name, user_id, deleted",
                        unique = true)}
)
public class FileEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",
            nullable = false,
            updatable = false)
    private UserEntity user;

    @Column(name = "file_name",
            nullable = false)
    private String fileName;

    @Column(name = "deleted",
            columnDefinition = "boolean default false")
    private boolean deleted;

    @Column(name = "size",
            nullable = false)
    private Long size;

    @CreationTimestamp
    @Column(name = "created_at",
            updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "full_path",
            nullable = false,
            unique = true)
    private String fullPath;

    @Column(name = "broken_file",
            columnDefinition = "boolean default false",
            nullable = false)
    private boolean broken;

    @Transient
    private String extention;
}
