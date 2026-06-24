package com.xarch.starter.db.util;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PageHelper}.
 *
 * <p>Validates page construction, conversion from {@link Page} (MyBatis-Flex)
 * to {@link PageResult} (framework), and the conditional query helpers.</p>
 */
@DisplayName("PageHelper Tests")
class PageHelperTest {

    @Nested
    @DisplayName("buildPage")
    class BuildPage {

        @Test
        @DisplayName("buildPage returns a Page with the requested page number and size")
        void buildPage_returnsPageWithGivenDimensions() {
            Page<String> page = PageHelper.buildPage(2, 25);

            assertThat(page).isNotNull();
            assertThat(page.getPageNumber()).isEqualTo(2);
            assertThat(page.getPageSize()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("toPageResult Conversions")
    class ToPageResultConversions {

        @Test
        @DisplayName("toPageResult(Page) converts records and totalRow")
        void toPageResult_fromPage_convertsRecordsAndTotal() {
            // Arrange
            List<Integer> records = Arrays.asList(1, 2, 3);
            Page<Integer> page = Page.of(1, 10);
            page.setRecords(records);
            page.setTotalRow(123L);

            // Act
            PageResult<Integer> result = PageHelper.toPageResult(page);

            // Assert
            assertThat(result.getList()).containsExactly(1, 2, 3);
            assertThat(result.getTotal()).isEqualTo(123L);
        }

        @Test
        @DisplayName("toPageResult(list, total) wraps the list and total")
        void toPageResult_fromListAndTotal_wrapsValues() {
            List<String> items = List.of("a", "b");
            PageResult<String> result = PageHelper.toPageResult(items, 7L);

            assertThat(result.getList()).containsExactly("a", "b");
            assertThat(result.getTotal()).isEqualTo(7L);
        }

        @Test
        @DisplayName("toPageResult of an empty list produces an empty PageResult")
        void toPageResult_emptyList_yieldsEmptyResult() {
            PageResult<String> result = PageHelper.toPageResult(Collections.emptyList(), 0L);

            assertThat(result.getList()).isEmpty();
            assertThat(result.getTotal()).isZero();
        }
    }

    @Nested
    @DisplayName("Conditional Query Helpers")
    class ConditionalQueries {

        @Test
        @DisplayName("addLikeIfPresent adds LIKE when value is non-blank")
        void addLikeIfPresent_addsLikeWhenValuePresent() {
            QueryWrapper wrapper = new QueryWrapper();
            QueryWrapper result = PageHelper.addLikeIfPresent(wrapper, "name", "abc");

            assertThat(result).isSameAs(wrapper);
            assertThat(result.toSQL()).containsIgnoringCase("like");
        }

        @Test
        @DisplayName("addLikeIfPresent skips LIKE when value is null")
        void addLikeIfPresent_skipsWhenValueNull() {
            QueryWrapper wrapper = new QueryWrapper();
            QueryWrapper result = PageHelper.addLikeIfPresent(wrapper, "name", null);

            assertThat(result.toSQL()).doesNotContainIgnoringCase("like");
        }

        @Test
        @DisplayName("addLikeIfPresent skips LIKE when value is blank")
        void addLikeIfPresent_skipsWhenValueBlank() {
            QueryWrapper wrapper = new QueryWrapper();
            QueryWrapper result = PageHelper.addLikeIfPresent(wrapper, "name", "   ");

            assertThat(result.toSQL()).doesNotContainIgnoringCase("like");
        }

        @Test
        @DisplayName("addEqIfPresent adds EQ when value is non-null")
        void addEqIfPresent_addsEqWhenValuePresent() {
            QueryWrapper wrapper = new QueryWrapper();
            QueryWrapper result = PageHelper.addEqIfPresent(wrapper, "id", 42L);

            assertThat(result.toSQL()).containsIgnoringCase("=");
        }

        @Test
        @DisplayName("addEqIfPresent skips EQ when value is null")
        void addEqIfPresent_skipsWhenValueNull() {
            QueryWrapper wrapper = new QueryWrapper();
            QueryWrapper result = PageHelper.addEqIfPresent(wrapper, "id", null);

            assertThat(result.toSQL()).doesNotContainIgnoringCase("=");
        }

        @Test
        @DisplayName("addEqIfPresent skips EQ when value is a blank string")
        void addEqIfPresent_skipsWhenValueBlankString() {
            QueryWrapper wrapper = new QueryWrapper();
            QueryWrapper result = PageHelper.addEqIfPresent(wrapper, "name", "   ");

            assertThat(result.toSQL()).doesNotContainIgnoringCase("=");
        }

        @Test
        @DisplayName("addEqIfPresent treats a non-blank string as a real value")
        void addEqIfPresent_addsEqForNonBlankString() {
            QueryWrapper wrapper = new QueryWrapper();
            QueryWrapper result = PageHelper.addEqIfPresent(wrapper, "name", "x");

            assertThat(result.toSQL()).containsIgnoringCase("=");
        }
    }
}