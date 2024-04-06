package ru.netology.diplomcloudstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
public class DiplomCloudstoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiplomCloudstoreApplication.class, args);
    }
}
