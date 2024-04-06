package ru.netology.diplomcloudstore.entities;

import jakarta.persistence.*;
import lombok.*;

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
        name = "users_jwt_blacklist",
        indexes = {
                @Index(
                        name = "atomic_jwt_index_1",
                        columnList = "jwt"),
                @Index(
                        name = "composite_jwt_index_1",
                        columnList = "user_id, revoke, expired")
        }
)
public class UserJwtTokenEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jwt;

    @Column(nullable = false)
    private boolean expired;

    @Column(nullable = false)
    private boolean revoke;

    @Column(name = "create_date")
    private Date createDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;
}
