package com.xarch.example.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Monitor service entry point. */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xarch.example.monitor.client")
@SpringBootApplication(scanBasePackages = "com.xarch.example.monitor")
@EnableTransactionManagement
@EnableScheduling
@MapperScan("com.xarch.example.monitor.mapper")
public class MonitorMicroApplication {

    /**
     * Application bootstrap.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MonitorMicroApplication.class, args);
    }
}