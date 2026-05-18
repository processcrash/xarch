package com.xarch.cloud.nacos.annotation;

import java.lang.annotation.*;

/**
 * Mark a Spring Bean as an MCP Server that should be registered with Nacos
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpServer {
    /**
     * MCP server name
     */
    String name();

    /**
     * MCP server type (database, knowledge, filesystem, etc.)
     */
    String type();

    /**
     * MCP server port
     */
    int port() default 9090;

    /**
     * MCP server version
     */
    String version() default "1.0.0";

    /**
     * Capabilities provided by this MCP server
     */
    String[] capabilities() default {};
}