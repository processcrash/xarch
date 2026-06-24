package com.xarch.starter.core.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PageResult}.
 *
 * <p>Validates constructors, factory methods and accessors for the page
 * response wrapper.</p>
 */
@DisplayName("PageResult Tests")
class PageResultTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("Default constructor yields empty list with zero total")
        void defaultConstructor_yieldsEmptyResult() {
            // Arrange & Act
            PageResult<String> result = new PageResult<>();

            // Assert
            assertThat(result.getList()).isNull();
            assertThat(result.getTotal()).isZero();
        }

        @Test
        @DisplayName("Parameterized constructor sets list and total")
        void parameterizedConstructor_setsListAndTotal() {
            // Arrange
            List<Integer> data = Arrays.asList(1, 2, 3);

            // Act
            PageResult<Integer> result = new PageResult<>(data, 100L);

            // Assert
            assertThat(result.getList()).containsExactly(1, 2, 3);
            assertThat(result.getTotal()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("of(list, total) constructs with provided values")
        void of_returnsResultWithProvidedValues() {
            // Arrange
            List<String> items = Arrays.asList("a", "b");

            // Act
            PageResult<String> result = PageResult.of(items, 50L);

            // Assert
            assertThat(result.getList()).isEqualTo(items);
            assertThat(result.getTotal()).isEqualTo(50L);
        }

        @Test
        @DisplayName("ok(list) sets total to list size")
        void ok_usesListSizeAsTotal() {
            // Arrange
            List<String> items = Arrays.asList("a", "b", "c");

            // Act
            PageResult<String> result = PageResult.ok(items);

            // Assert
            assertThat(result.getList()).hasSize(3);
            assertThat(result.getTotal()).isEqualTo(3L);
        }

        @Test
        @DisplayName("ok(null) returns total 0 and null list")
        void ok_withNull_returnsZeroTotal() {
            // Act
            PageResult<String> result = PageResult.ok(null);

            // Assert
            assertThat(result.getList()).isNull();
            assertThat(result.getTotal()).isZero();
        }

        @Test
        @DisplayName("of(emptyList, 0) constructs empty page result")
        void of_emptyList_returnsEmptyResult() {
            // Act
            PageResult<String> result = PageResult.of(Collections.emptyList(), 0L);

            // Assert
            assertThat(result.getList()).isEmpty();
            assertThat(result.getTotal()).isZero();
        }
    }

    @Nested
    @DisplayName("Accessors")
    class Accessors {

        @Test
        @DisplayName("Mutators reflect newly assigned values")
        void setters_updateFieldsCorrectly() {
            // Arrange
            PageResult<String> result = new PageResult<>();
            List<String> newList = List.of("x", "y");

            // Act
            result.setList(newList);
            result.setTotal(75L);

            // Assert
            assertThat(result.getList()).isEqualTo(newList);
            assertThat(result.getTotal()).isEqualTo(75L);
        }
    }
}