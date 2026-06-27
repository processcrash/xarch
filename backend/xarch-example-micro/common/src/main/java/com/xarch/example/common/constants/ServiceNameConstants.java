package com.xarch.example.common.constants;

/**
 * Constants for Spring Cloud service names registered in Nacos.
 *
 * <p>Use these constants when referencing a peer service from
 * {@code @FeignClient(name = ...)} or any place that needs the
 * canonical Spring application name. Keeping the names in a single
 * place makes renaming or refactoring safer.
 */
public final class ServiceNameConstants {

    /** Authentication & user service (port 9001). */
    public static final String XARCH_SERVICE_AUTH = "xarch-service-auth";

    /** System management service: roles/menus/depts/dicts/config/logs (port 9002). */
    public static final String XARCH_SERVICE_SYSTEM = "xarch-service-system";

    /** File management service: storage, resource, excel (port 9003). */
    public static final String XARCH_SERVICE_FILE = "xarch-service-file";

    /** Monitor service: server info, cache, scheduled jobs (port 9004). */
    public static final String XARCH_SERVICE_MONITOR = "xarch-service-monitor";

    /** AI service: chat, MCP, RAG, server command (port 9005). */
    public static final String XARCH_SERVICE_AI = "xarch-service-ai";

    /** Messaging service: notifications and OAuth/SSO clients (port 9006). */
    public static final String XARCH_SERVICE_MESSAGE = "xarch-service-message";

    private ServiceNameConstants() {
        // utility class — not instantiable
    }
}