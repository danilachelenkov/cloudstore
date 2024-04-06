package ru.netology.diplomcloudstore.configurations;

import jdk.jfr.Description;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "project.filesystem")
@Description("Bean для инициализации кастомных параметров настройки проекта")
public class ProjectPropertyConfiguration {
    private String type;
}
