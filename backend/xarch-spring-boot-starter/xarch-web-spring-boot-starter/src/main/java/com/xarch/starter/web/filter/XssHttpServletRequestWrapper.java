package com.xarch.starter.web.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * XSS 过滤请求包装器
 * 对请求参数进行 XSS 字符转义
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] sanitizedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitizedValues[i] = sanitize(values[i]);
        }
        return sanitizedValues;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitize(value);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> rawMap = super.getParameterMap();
        if (rawMap == null) {
            return null;
        }
        Map<String, String[]> sanitizedMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : rawMap.entrySet()) {
            String[] rawValues = entry.getValue();
            if (rawValues != null) {
                String[] sanitizedValues = new String[rawValues.length];
                for (int i = 0; i < rawValues.length; i++) {
                    sanitizedValues[i] = sanitize(rawValues[i]);
                }
                sanitizedMap.put(entry.getKey(), sanitizedValues);
            } else {
                sanitizedMap.put(entry.getKey(), rawValues);
            }
        }
        return sanitizedMap;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return sanitize(value);
    }

    private String sanitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        // 使用 XssFilter 的转义方法
        return XssFilter.sanitize(value);
    }
}