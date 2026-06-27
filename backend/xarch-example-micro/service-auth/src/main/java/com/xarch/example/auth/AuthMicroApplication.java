package com.xarch.example.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot entry point for {@code service-auth}.
 *
 * <p>Wires up:
 * <ul>
 *   <li>Nacos discovery via {@link EnableDiscoveryClient}</li>
 *   <li>OpenFeign client scanning rooted at {@code com.xarch.example.auth.client}</li>
 *   <li>MyBatis-Flex mapper scanning on {@code com.xarch.example.auth.mapper}</li>
 *   <li>Annotation-driven transaction management</li>
 * </ul>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xarch.example.auth.client")
@SpringBootApplication(scanBasePackages = "com.xarch.example.auth")
@EnableTransactionManagement
@MapperScan("com.xarch.example.auth.mapper")
public class AuthMicroApplication {

    /**
     * Application bootstrap.
     *
     * @param args command-line arguments forwarded to Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthMicroApplication.class, args);
    }
}