package com.xarch.starter.core.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PageQuery}.
 *
 * <p>Verifies the default values and normalization logic of the pageNum /
 * pageSize accessors (which clamp invalid values to safe defaults).</p>
 */
@DisplayName("PageQuery Tests")
class PageQueryTest {

    @Nested
    @DisplayName("Defaults")
    class Defaults {

        @Test
        @DisplayName("pageNum default is 1")
        void defaultPageNum_isOne() {
            assertThat(new PageQuery().getPageNum()).isEqualTo(1);
        }

        @Test
        @DisplayName("pageSize default is 10")
        void defaultPageSize_isTen() {
            assertThat(new PageQuery().getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("orderDirection default is 'desc'")
        void defaultOrderDirection_isDesc() {
            assertThat(new PageQuery().getOrderDirection()).isEqualTo("desc");
        }

        @Test
        @DisplayName("orderBy default is null")
        void defaultOrderBy_isNull() {
            assertThat(new PageQuery().getOrderBy()).isNull();
        }
    }

    @Nested
    @DisplayName("PageNum Normalization")
    class PageNumNormalization {

        @Test
        @DisplayName("Negative pageNum is clamped to 1")
        void negativePageNum_isClampedToOne() {
            PageQuery query = new PageQuery();
            query.setPageNum(-5);

            assertThat(query.getPageNum()).isEqualTo(1);
        }

        @Test
        @DisplayName("Zero pageNum is clamped to 1")
        void zeroPageNum_isClampedToOne() {
            PageQuery query = new PageQuery();
            query.setPageNum(0);

            assertThat(query.getPageNum()).isEqualTo(1);
        }

        @Test
        @DisplayName("Null pageNum is normalized to 1")
        void nullPageNum_isNormalizedToOne() {
            PageQuery query = new PageQuery();
            query.setPageNum(null);

            assertThat(query.getPageNum()).isEqualTo(1);
        }

        @Test
        @DisplayName("Positive pageNum is returned as-is")
        void positivePageNum_isReturnedAsIs() {
            PageQuery query = new PageQuery();
            query.setPageNum(7);

            assertThat(query.getPageNum()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("PageSize Normalization")
    class PageSizeNormalization {

        @Test
        @DisplayName("Negative pageSize is clamped to 10")
        void negativePageSize_isClampedToTen() {
            PageQuery query = new PageQuery();
            query.setPageSize(-1);

            assertThat(query.getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Zero pageSize is clamped to 10")
        void zeroPageSize_isClampedToTen() {
            PageQuery query = new PageQuery();
            query.setPageSize(0);

            assertThat(query.getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("pageSize greater than 100 is clamped to 100")
        void pageSizeGreaterThan100_isClampedToHundred() {
            PageQuery query = new PageQuery();
            query.setPageSize(500);

            assertThat(query.getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("pageSize equal to 100 is allowed")
        void pageSizeEqualTo100_isAllowed() {
            PageQuery query = new PageQuery();
            query.setPageSize(100);

            assertThat(query.getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("Null pageSize is normalized to 10")
        void nullPageSize_isNormalizedToTen() {
            PageQuery query = new PageQuery();
            query.setPageSize(null);

            assertThat(query.getPageSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Setter Roundtrip")
    class SetterRoundtrip {

        @Test
        @DisplayName("orderBy and orderDirection are round-tripped via setters")
        void orderByAndDirection_areRoundtripped() {
            PageQuery query = new PageQuery();
            query.setOrderBy("created_at");
            query.setOrderDirection("asc");

            assertThat(query.getOrderBy()).isEqualTo("created_at");
            assertThat(query.getOrderDirection()).isEqualTo("asc");
        }
    }
}