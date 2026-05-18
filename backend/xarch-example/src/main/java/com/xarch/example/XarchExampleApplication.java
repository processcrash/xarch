package com.xarch.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example application
 */
@SpringBootApplication
@MapperScan("com.xarch.example.mapper")
public class XarchExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(XarchExampleApplication.class, args);
    }
}