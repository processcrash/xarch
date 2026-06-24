package com.xarch.starter.web.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XssHttpServletRequestWrapper}.
 *
 * <p>Verifies that parameters, parameter maps, and headers returned by the
 * wrapper have been sanitized via {@link XssFilter#sanitize(String)}.</p>
 */
@DisplayName("XssHttpServletRequestWrapper Tests")
class XssHttpServletRequestWrapperTest {

    private HttpServletRequest delegate;
    private XssHttpServletRequestWrapper wrapper;

    @BeforeEach
    void setUp() {
        delegate = mock(HttpServletRequest.class);
        wrapper = new XssHttpServletRequestWrapper(delegate);
    }

    @Test
    @DisplayName("getParameter sanitizes a single parameter value")
    void getParameter_sanitizesValue() {
        when(delegate.getParameter("payload")).thenReturn("<script>alert(1)</script>");

        String sanitized = wrapper.getParameter("payload");

        assertThat(sanitized).doesNotContain("<");
        assertThat(sanitized).contains("&lt;script&gt;");
    }

    @Test
    @DisplayName("getParameter returns null when the underlying value is null")
    void getParameter_returnsNullWhenDelegateNull() {
        when(delegate.getParameter("missing")).thenReturn(null);

        assertThat(wrapper.getParameter("missing")).isNull();
    }

    @Test
    @DisplayName("getParameterValues sanitizes every value in the array")
    void getParameterValues_sanitizesAllValues() {
        when(delegate.getParameterValues("multi")).thenReturn(new String[]{"<a>", "<b>"});

        String[] sanitized = wrapper.getParameterValues("multi");

        assertThat(sanitized).containsExactly("&lt;a&gt;", "&lt;b&gt;");
    }

    @Test
    @DisplayName("getParameterValues returns null when the underlying array is null")
    void getParameterValues_returnsNullWhenDelegateNull() {
        when(delegate.getParameterValues("missing")).thenReturn(null);

        assertThat(wrapper.getParameterValues("missing")).isNull();
    }

    @Test
    @DisplayName("getParameterMap sanitizes each value in the map")
    void getParameterMap_sanitizesEachValue() {
        Map<String, String[]> raw = new HashMap<>();
        raw.put("k", new String[]{"<x>"});
        when(delegate.getParameterMap()).thenReturn(raw);

        Map<String, String[]> sanitized = wrapper.getParameterMap();

        assertThat(sanitized).containsKey("k");
        assertThat(sanitized.get("k")).containsExactly("&lt;x&gt;");
    }

    @Test
    @DisplayName("getParameterMap returns null when the underlying map is null")
    void getParameterMap_returnsNullWhenDelegateNull() {
        when(delegate.getParameterMap()).thenReturn(null);

        assertThat(wrapper.getParameterMap()).isNull();
    }

    @Test
    @DisplayName("getParameterMap preserves null values")
    void getParameterMap_preservesNullValues() {
        Map<String, String[]> raw = new HashMap<>();
        raw.put("k", null);
        when(delegate.getParameterMap()).thenReturn(raw);

        Map<String, String[]> sanitized = wrapper.getParameterMap();

        assertThat(sanitized.get("k")).isNull();
    }

    @Test
    @DisplayName("getHeader sanitizes the header value")
    void getHeader_sanitizesValue() {
        when(delegate.getHeader("X-Test")).thenReturn("<script>");

        String sanitized = wrapper.getHeader("X-Test");

        assertThat(sanitized).doesNotContain("<");
    }

    @Test
    @DisplayName("getHeader returns null when the underlying header is null")
    void getHeader_returnsNullWhenDelegateNull() {
        when(delegate.getHeader("Missing")).thenReturn(null);

        assertThat(wrapper.getHeader("Missing")).isNull();
    }

    @Test
    @DisplayName("Empty input is preserved as-is")
    void emptyValues_arePreserved() {
        when(delegate.getParameter("p")).thenReturn("");

        assertThat(wrapper.getParameter("p")).isEmpty();
    }
}