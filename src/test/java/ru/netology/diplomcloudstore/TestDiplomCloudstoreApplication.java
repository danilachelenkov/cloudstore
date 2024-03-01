package ru.netology.diplomcloudstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestDiplomCloudstoreApplication {

    public static void main(String[] args) {
        SpringApplication.from(DiplomCloudstoreApplication::main).with(TestDiplomCloudstoreApplication.class).run(args);
    }

}
