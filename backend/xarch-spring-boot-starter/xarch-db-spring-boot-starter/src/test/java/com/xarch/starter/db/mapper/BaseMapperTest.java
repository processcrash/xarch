package com.xarch.starter.db.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BaseMapper}.
 *
 * <p>Verifies the convenience default methods delegate to the corresponding
 * MyBatis-Flex {@link com.mybatisflex.core.BaseMapper} APIs.</p>
 */
@DisplayName("BaseMapper Tests")
class BaseMapperTest {

    @SuppressWarnings("unchecked")
    private BaseMapper<String> mapper;

    @BeforeEach
    void setUp() {
        mapper = (BaseMapper<String>) mock(BaseMapper.class);
    }

    @Nested
    @DisplayName("insertAndGetId")
    class InsertAndGetId {

        @Test
        @DisplayName("insertAndGetId delegates to insert and returns its result")
        void insertAndGetId_delegatesToInsert() {
            // Arrange
            when(mapper.insert("payload")).thenReturn(1);

            // Act
            int result = mapper.insertAndGetId("payload");

            // Assert
            assertThat(result).isEqualTo(1);
            verify(mapper).insert("payload");
        }
    }

    @Nested
    @DisplayName("selectById")
    class SelectById {

        @Test
        @DisplayName("selectById delegates to selectOneById and returns its result")
        void selectById_delegatesToSelectOneById() {
            // Arrange
            when(mapper.selectOneById(7L)).thenReturn("row");

            // Act
            String result = mapper.selectById(7L);

            // Assert
            assertThat(result).isEqualTo("row");
            verify(mapper).selectOneById(7L);
        }
    }

    @Nested
    @DisplayName("updateById")
    class UpdateById {

        @Test
        @DisplayName("updateById delegates to update and returns its result")
        void updateById_delegatesToUpdate() {
            // Arrange
            when(mapper.update("payload")).thenReturn(1);

            // Act
            int result = mapper.updateById("payload");

            // Assert
            assertThat(result).isEqualTo(1);
            verify(mapper).update("payload");
        }
    }

    @Nested
    @DisplayName("selectList")
    class SelectList {

        @Test
        @DisplayName("selectList uses an empty QueryWrapper and returns the records")
        void selectList_returnsRecords() {
            // Arrange
            List<String> rows = Arrays.asList("a", "b", "c");
            when(mapper.selectListByQuery(QueryWrapper.create())).thenReturn(rows);

            // Act
            List<String> result = mapper.selectList();

            // Assert
            assertThat(result).containsExactly("a", "b", "c");
            verify(mapper).selectListByQuery(QueryWrapper.create());
        }

        @Test
        @DisplayName("selectList returns an empty list when nothing matches")
        void selectList_returnsEmptyWhenNoMatches() {
            // Arrange
            when(mapper.selectListByQuery(QueryWrapper.create())).thenReturn(List.of());

            // Act
            List<String> result = mapper.selectList();

            // Assert
            assertThat(result).isEmpty();
        }
    }
}