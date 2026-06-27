package com.xarch.example.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** AI service entry point. */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xarch.example.ai.client")
@SpringBootApplication(scanBasePackages = "com.xarch.example.ai")
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@MapperScan("com.xarch.example.ai.mapper")
public class AiMicroApplication {

    /**
     * Application bootstrap.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AiMicroApplication.class, args);
    }
}