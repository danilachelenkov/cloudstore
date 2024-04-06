package ru.netology.diplomcloudstore.configurations;

import jdk.jfr.Description;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "cors.settings")
@Description("Bean для инициализации кастомных параметров настройки Cors проекта")
public class CorsPropertyConfiguration {
    private String mapping;
    private boolean credentials;
    private String origins;
    private String headers;
    List<String> methods;

    public String[] getArrayMethods(){
        return methods.toArray(new String[methods.size()]);
    }
}
