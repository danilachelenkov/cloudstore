package ru.netology.diplomcloudstore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import ru.netology.diplomcloudstore.entities.SettingEntity;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface SettingRepository extends JpaRepository<SettingEntity, Integer> {
    List<SettingEntity> findAllByActive(Boolean isActive);
}
