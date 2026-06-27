package com.xarch.example.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** File service entry point. */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xarch.example.file.client")
@SpringBootApplication(scanBasePackages = "com.xarch.example.file")
@EnableTransactionManagement
@MapperScan("com.xarch.example.file.mapper")
public class FileMicroApplication {

    /**
     * Application bootstrap.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FileMicroApplication.class, args);
    }
}