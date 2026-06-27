package com.xarch.example.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot entry point for {@code service-system}.
 *
 * <p>Hosts RBAC + audit + notice + post + online-user endpoints.
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xarch.example.system.client")
@SpringBootApplication(scanBasePackages = "com.xarch.example.system")
@EnableTransactionManagement
@MapperScan("com.xarch.example.system.mapper")
public class SystemMicroApplication {

    /**
     * Application bootstrap.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SystemMicroApplication.class, args);
    }
}