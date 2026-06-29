package com.xarch.cms;

import com.xarch.cms.entity.Category;
import com.xarch.cms.exception.CmsException;
import com.xarch.cms.mapper.CategoryMapper;
import com.xarch.cms.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CategoryServiceImpl}. Covers CRUD, list ordering
 * and tree assembly.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should default parentId to 0 when null")
        void shouldDefaultParentId_whenNull() {
            Category category = Category.builder().name("Root").build();

            categoryService.create(category);

            assertThat(category.getParentId()).isEqualTo(0L);
            verify(categoryMapper).insert(category);
        }

        @Test
        @DisplayName("should respect provided parentId")
        void shouldRespectParentId_whenProvided() {
            Category category = Category.builder().name("Child").parentId(5L).build();

            categoryService.create(category);

            assertThat(category.getParentId()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("list")
    class ListAll {

        @Test
        @DisplayName("should return all categories sorted by sortOrder")
        void shouldReturnAllSorted() {
            Category a = Category.builder().id(1L).name("A").sortOrder(2).build();
            Category b = Category.builder().id(2L).name("B").sortOrder(1).build();
            Category c = Category.builder().id(3L).name("C").sortOrder(3).build();
            when(categoryMapper.selectList()).thenReturn(List.of(a, b, c));

            List<Category> result = categoryService.list();

            assertThat(result).extracting(Category::getId).containsExactly(2L, 1L, 3L);
        }

        @Test
        @DisplayName("should return empty list when mapper returns empty")
        void shouldReturnEmpty_whenNoCategories() {
            when(categoryMapper.selectList()).thenReturn(List.of());

            List<Category> result = categoryService.list();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("tree")
    class Tree {

        @Test
        @DisplayName("should build nested tree from flat list")
        void shouldBuildTree() {
            Category root1 = Category.builder().id(1L).name("Root1").parentId(0L).sortOrder(1).build();
            Category root2 = Category.builder().id(2L).name("Root2").parentId(0L).sortOrder(2).build();
            Category child1 = Category.builder().id(3L).name("Child1").parentId(1L).sortOrder(1).build();
            Category grandchild = Category.builder().id(4L).name("Grand").parentId(3L).sortOrder(1).build();
            when(categoryMapper.selectList()).thenReturn(List.of(root1, root2, child1, grandchild));

            List<Category> tree = categoryService.tree();

            assertThat(tree).hasSize(2);
            Category first = tree.get(0);
            assertThat(first.getId()).isEqualTo(1L);
            assertThat(first.getChildren()).hasSize(1);
            Category firstChild = first.getChildren().get(0);
            assertThat(firstChild.getId()).isEqualTo(3L);
            assertThat(firstChild.getChildren()).hasSize(1);
            assertThat(firstChild.getChildren().get(0).getId()).isEqualTo(4L);
        }

        @Test
        @DisplayName("should treat null parent as root")
        void shouldTreatNullParentAsRoot() {
            Category orphan = Category.builder().id(1L).name("Orphan").parentId(null).sortOrder(1).build();
            when(categoryMapper.selectList()).thenReturn(List.of(orphan));

            List<Category> tree = categoryService.tree();

            assertThat(tree).hasSize(1);
            assertThat(tree.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should treat orphan nodes (parent missing) as root")
        void shouldTreatOrphansAsRoot() {
            Category orphan = Category.builder().id(1L).name("Orphan").parentId(99L).sortOrder(1).build();
            when(categoryMapper.selectList()).thenReturn(List.of(orphan));

            List<Category> tree = categoryService.tree();

            assertThat(tree).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update / delete / getById")
    class Crud {

        @Test
        @DisplayName("should update by id")
        void shouldUpdate() {
            Category category = Category.builder().id(1L).name("Updated").build();
            categoryService.update(category);
            verify(categoryMapper).updateById(category);
        }

        @Test
        @DisplayName("should delete and reparent children")
        void shouldDeleteAndReparent() {
            Category self = Category.builder().id(1L).name("Self").parentId(0L).build();
            Category child = Category.builder().id(2L).name("Child").parentId(1L).build();
            Category other = Category.builder().id(3L).name("Other").parentId(0L).build();
            when(categoryMapper.selectOneById(1L)).thenReturn(self);
            when(categoryMapper.selectList()).thenReturn(List.of(self, child, other));

            categoryService.delete(1L);

            assertThat(child.getParentId()).isEqualTo(0L);
            verify(categoryMapper).updateById(child);
            verify(categoryMapper).deleteById(1L);
        }

        @Test
        @DisplayName("should throw on delete when category not found")
        void shouldThrowOnDelete_whenMissing() {
            when(categoryMapper.selectOneById(1L)).thenReturn(null);

            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOf(CmsException.class)
                    .hasMessageContaining("Category not found");
            verify(categoryMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("should return category by id")
        void shouldGetById() {
            Category category = Category.builder().id(1L).name("X").build();
            when(categoryMapper.selectOneById(1L)).thenReturn(category);

            Category found = categoryService.getById(1L);

            assertThat(found).isEqualTo(category);
        }
    }
}
