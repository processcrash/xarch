/*
 * xarch Admin Server
 * Spring Boot Admin Centralized Monitoring Server
 *
 * Access the admin UI at: http://localhost:8090/
 *
 * Start command:
 *   java -jar xarch-cloud-admin-server.jar
 *
 * Or add to your Spring Cloud application as a dependency and annotate with @EnableAdminServer
 */
package com.xarch.cloud.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Admin Server Application
 *
 * Provides centralized monitoring dashboard for all registered services.
 *
 * Features:
 * - Service registration via Spring Cloud / Nacos
 * - Health status monitoring
 * - Metrics and performance monitoring
 * - Log stream real-time viewing
 * - Thread and heap dump collection
 * - Environment and configuration inspection
 * - Event journal for application lifecycle events
 */
@SpringBootApplication
@EnableAdminServer
public class AdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}