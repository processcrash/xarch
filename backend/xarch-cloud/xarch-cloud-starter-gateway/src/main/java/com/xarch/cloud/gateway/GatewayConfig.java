package com.xarch.cloud.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API Gateway Configuration
 * Routes requests to backend services and MCP servers
 */
@Configuration
public class GatewayConfig {

    /**
     * Configure API routes
     * - /api/system/* -> xarch-example (user, role, menu, dept, etc.)
     * - /api/monitor/* -> xarch-example (logs, online, server, cache)
     * - /mcp/* -> MCP servers (database, knowledge, filesystem)
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // System management routes
            .route("system-user", r -> r.path("/api/system/user/**")
                .uri("lb://xarch-example"))
            .route("system-role", r -> r.path("/api/system/role/**")
                .uri("lb://xarch-example"))
            .route("system-menu", r -> r.path("/api/system/menu/**")
                .uri("lb://xarch-example"))
            .route("system-dept", r -> r.path("/api/system/dept/**")
                .uri("lb://xarch-example"))
            .route("system-post", r -> r.path("/api/system/post/**")
                .uri("lb://xarch-example"))
            .route("system-notice", r -> r.path("/api/system/notice/**")
                .uri("lb://xarch-example"))
            .route("system-dict", r -> r.path("/api/system/dict/**")
                .uri("lb://xarch-example"))
            .route("system-config", r -> r.path("/api/system/config/**")
                .uri("lb://xarch-example"))

            // Monitor routes
            .route("monitor-logininfor", r -> r.path("/api/monitor/logininfor/**")
                .uri("lb://xarch-example"))
            .route("monitor-operlog", r -> r.path("/api/monitor/operlog/**")
                .uri("lb://xarch-example"))
            .route("monitor-online", r -> r.path("/api/monitor/online/**")
                .uri("lb://xarch-example"))
            .route("monitor-server", r -> r.path("/api/monitor/server/**")
                .uri("lb://xarch-example"))
            .route("monitor-cache", r -> r.path("/api/monitor/cache/**")
                .uri("lb://xarch-example"))
            .route("monitor-job", r -> r.path("/api/monitor/job/**")
                .uri("lb://xarch-example"))
            .route("monitor-jobLog", r -> r.path("/api/monitor/jobLog/**")
                .uri("lb://xarch-example"))

            // MCP Server routes
            .route("mcp-database", r -> r.path("/mcp/database/**")
                .uri("lb://xarch-mcp-database"))
            .route("mcp-knowledge", r -> r.path("/mcp/knowledge/**")
                .uri("lb://xarch-mcp-knowledge"))
            .route("mcp-filesystem", r -> r.path("/mcp/filesystem/**")
                .uri("lb://xarch-mcp-filesystem"))

            // Auth routes
            .route("auth-login", r -> r.path("/api/auth/login/**")
                .uri("lb://xarch-example"))
            .route("auth-captcha", r -> r.path("/api/auth/captcha/**")
                .uri("lb://xarch-example"))

            .build();
    }
}