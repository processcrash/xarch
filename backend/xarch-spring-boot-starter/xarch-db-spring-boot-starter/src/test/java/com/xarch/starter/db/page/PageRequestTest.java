package com.xarch.starter.db.page;

import com.xarch.starter.core.entity.PageQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PageRequest}.
 *
 * <p>Validates inherited {@link PageQuery} behavior and the {@code params}
 * property round-trip.</p>
 */
@DisplayName("PageRequest Tests")
class PageRequestTest {

    @Nested
    @DisplayName("Inherited PageQuery Behavior")
    class InheritedBehavior {

        @Test
        @DisplayName("Default pageNum is 1")
        void defaultPageNum_isOne() {
            assertThat(new PageRequest<String>().getPageNum()).isEqualTo(1);
        }

        @Test
        @DisplayName("Default pageSize is 10")
        void defaultPageSize_isTen() {
            assertThat(new PageRequest<String>().getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Page size is clamped to 100 max")
        void pageSize_isClampedToMax() {
            PageRequest<String> request = new PageRequest<>();
            request.setPageSize(999);

            assertThat(request.getPageSize()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Params Property")
    class ParamsProperty {

        @Test
        @DisplayName("params is null by default")
        void params_defaultIsNull() {
            assertThat(new PageRequest<String>().getParams()).isNull();
        }

        @Test
        @DisplayName("params getter/setter round-trip")
        void params_roundtrips() {
            PageRequest<String> request = new PageRequest<>();
            request.setParams("hello");

            assertThat(request.getParams()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Generic params type is preserved")
        void params_genericTypePreserved() {
            PageRequest<Integer> request = new PageRequest<>();
            request.setParams(42);

            assertThat(request.getParams()).isEqualTo(42);
        }
    }
}