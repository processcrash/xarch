package com.xarch.starter.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XssFilter}.
 *
 * <p>Exercises the static XSS detection/sanitization helpers and verifies that
 * the filter wraps the incoming request before passing it down the chain.</p>
 */
@DisplayName("XssFilter Tests")
class XssFilterTest {

    private XssFilter filter;

    @BeforeEach
    void setUp() {
        filter = new XssFilter();
    }

    @Nested
    @DisplayName("Static XSS Detection")
    class StaticDetection {

        @Test
        @DisplayName("hasXssRisk returns true for &lt;script&gt; tags")
        void hasXssRisk_detectsScriptTags() {
            assertThat(XssFilter.hasXssRisk("<script>alert(1)</script>")).isTrue();
        }

        @Test
        @DisplayName("hasXssRisk returns true for inline event handlers")
        void hasXssRisk_detectsEventHandlers() {
            assertThat(XssFilter.hasXssRisk("onerror=alert(1)")).isTrue();
        }

        @Test
        @DisplayName("hasXssRisk returns true for javascript: URIs")
        void hasXssRisk_detectsJavascriptUri() {
            assertThat(XssFilter.hasXssRisk("javascript:alert(1)")).isTrue();
        }

        @Test
        @DisplayName("hasXssRisk returns true for &lt;iframe&gt; tags")
        void hasXssRisk_detectsIframeTags() {
            assertThat(XssFilter.hasXssRisk("<iframe src='evil.html'></iframe>")).isTrue();
        }

        @Test
        @DisplayName("hasXssRisk returns false for clean input")
        void hasXssRisk_returnsFalseForCleanInput() {
            assertThat(XssFilter.hasXssRisk("hello world")).isFalse();
        }

        @Test
        @DisplayName("hasXssRisk returns false for null or empty input")
        void hasXssRisk_returnsFalseForNullOrEmpty() {
            assertThat(XssFilter.hasXssRisk(null)).isFalse();
            assertThat(XssFilter.hasXssRisk("")).isFalse();
        }
    }

    @Nested
    @DisplayName("Static Sanitization")
    class StaticSanitization {

        @Test
        @DisplayName("sanitize escapes &lt;, &gt;, &quot;, &apos;, and / characters")
        void sanitize_escapesSpecialCharacters() {
            String result = XssFilter.sanitize("<script>alert(\"x\")</script>/'");

            assertThat(result).doesNotContain("<");
            assertThat(result).doesNotContain(">");
            assertThat(result).doesNotContain("\"");
            assertThat(result).doesNotContain("'");
            assertThat(result).doesNotContain("/");
        }

        @Test
        @DisplayName("sanitize returns null for null input")
        void sanitize_returnsNullForNull() {
            assertThat(XssFilter.sanitize(null)).isNull();
        }

        @Test
        @DisplayName("sanitize returns empty string for empty input")
        void sanitize_returnsEmptyForEmpty() {
            assertThat(XssFilter.sanitize("")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Filter Chain Behavior")
    class FilterChainBehavior {

        @Test
        @DisplayName("doFilter wraps the request and delegates to the chain")
        void doFilter_wrapsRequestAndDelegates() throws Exception {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            when(request.getParameter(any())).thenReturn("safe");

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(any(XssHttpServletRequestWrapper.class), any(HttpServletResponse.class));
        }
    }
}