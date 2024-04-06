package ru.netology.diplomcloudstore.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//Оставил для примера. Отключил в конфигурации. Все создается через liquibase
@Table(
        name = "settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"setting_name", "setting_value, is_active"}
                )
        }
)
public class SettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "setting_name", unique = true)
    private String settingName;

    @Column(name = "setting_value")
    private String settingValue;

    @Column(name = "is_active")
    private boolean active;
}
