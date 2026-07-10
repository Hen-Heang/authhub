package com.test.todoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.test.todoapi.domain",
        "com.henheang.securityapi.domain"
})
@ComponentScan(basePackages = {
        "com.test.todoapi",
        "com.henheang.securityapi",
        "com.henheang.commonapi"
})
@EnableJpaRepositories(basePackages = {
        "com.test.todoapi.repository",
        "com.henheang.securityapi.repository"
})
public class TodoapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoapiApplication.class, args);
    }

}
