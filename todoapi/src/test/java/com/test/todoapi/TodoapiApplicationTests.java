package com.test.todoapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootApplication
@EntityScan(basePackages = {"com.test.todoapi.domain", "com.henheang.authhub.domain"})
@SpringBootTest
class TodoapiApplicationTests {

    @Test
    void contextLoads() {
    }
}