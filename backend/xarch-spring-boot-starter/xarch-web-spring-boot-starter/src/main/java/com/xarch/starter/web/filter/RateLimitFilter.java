package com.xarch.starter.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流过滤器
 * 基于令牌桶算法的简单限流实现
 * 支持按 IP 进行限流控制
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter implements Filter {

    // 限流配置：每个 IP 每分钟最多请求次数
    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;

    // 存储每个 IP 的请求计数
    private final ConcurrentHashMap<String, RateLimitEntry> ipCounter = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);
        RateLimitEntry entry = ipCounter.computeIfAbsent(clientIp, k -> new RateLimitEntry());

        long now = System.currentTimeMillis();
        entry.cleanup(now);

        if (entry.count.get() >= MAX_REQUESTS_PER_MINUTE) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":\"1005\",\"msg\":\"请求过于频繁，请稍后再试\"}");
            return;
        }

        entry.count.incrementAndGet();
        entry.windowStart = now;

        // 添加限流头信息
        httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(MAX_REQUESTS_PER_MINUTE - entry.count.get()));

        chain.doFilter(request, response);
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个 IP（经过代理），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 限流条目
     */
    private static class RateLimitEntry {
        AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();

        void cleanup(long now) {
            if (now - windowStart > MINUTE_IN_MILLIS) {
                count.set(0);
                windowStart = now;
            }
        }
    }
}