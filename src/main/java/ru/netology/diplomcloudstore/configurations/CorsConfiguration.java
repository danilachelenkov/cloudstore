package ru.netology.diplomcloudstore.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfiguration implements WebMvcConfigurer {
    private final CorsPropertyConfiguration corsSettings;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(corsSettings.getMapping())
                .allowCredentials(corsSettings.isCredentials())
                .allowedOrigins(corsSettings.getOrigins())
                .allowedMethods(corsSettings.getArrayMethods())
                .allowedHeaders(corsSettings.getHeaders());
    }
}