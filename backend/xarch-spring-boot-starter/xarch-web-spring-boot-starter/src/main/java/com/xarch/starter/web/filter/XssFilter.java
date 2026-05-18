package com.xarch.starter.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * XSS 防护过滤器
 * 过滤 HTML 标签和危险字符，防止 XSS 攻击
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {

    // 危险 HTML 标签模式
    private static final Pattern[] DANGEROUS_PATTERNS = {
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("javascript:"),
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<meta[^>]*>", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 包装请求进行 XSS 过滤
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpRequest);
        chain.doFilter(xssRequest, response);
    }

    /**
     * 检查是否存在 XSS 风险
     */
    public static boolean hasXssRisk(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清除 XSS 风险字符
     */
    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String sanitized = value;
        // 转义 HTML 特殊字符
        sanitized = sanitized.replace("<", "&lt;");
        sanitized = sanitized.replace(">", "&gt;");
        sanitized = sanitized.replace("\"", "&quot;");
        sanitized = sanitized.replace("'", "&#x27;");
        sanitized = sanitized.replace("/", "&#x2F;");
        return sanitized;
    }
}