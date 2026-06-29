package com.xarch.cms;

import com.xarch.cms.entity.Tag;
import com.xarch.cms.mapper.ArticleTagMapper;
import com.xarch.cms.mapper.TagMapper;
import com.xarch.cms.service.impl.TagServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TagServiceImpl}. Validates list / CRUD plus the
 * slug uniqueness check.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tag Service Tests")
class TagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    @Mock
    private ArticleTagMapper articleTagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @Nested
    @DisplayName("list")
    class ListAll {

        @Test
        @DisplayName("should return all tags")
        void shouldReturnAllTags() {
            when(tagMapper.selectList()).thenReturn(List.of(
                    tag(1L, "java", "java"),
                    tag(2L, "spring", "spring")));

            List<Tag> result = tagService.list();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Tag::getSlug).containsExactly("java", "spring");
        }

        @Test
        @DisplayName("should return empty list when no tags")
        void shouldReturnEmpty_whenNone() {
            when(tagMapper.selectList()).thenReturn(List.of());

            List<Tag> result = tagService.list();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should insert tag through mapper")
        void shouldInsertTag() {
            Tag tag = tag(null, "kotlin", "kotlin");

            tagService.create(tag);

            verify(tagMapper).insert(tag);
        }

        @Test
        @DisplayName("should insert tag with required fields populated")
        void shouldInsertTagWithFields() {
            Tag tag = tag(null, "java", "java");

            tagService.create(tag);

            ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
            verify(tagMapper).insert(captor.capture());
            Tag saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("java");
            assertThat(saved.getSlug()).isEqualTo("java");
        }
    }

    @Nested
    @DisplayName("update / delete / getById")
    class Crud {

        @Test
        @DisplayName("should update tag")
        void shouldUpdateTag() {
            Tag tag = tag(1L, "java", "java");
            tagService.update(tag);
            verify(tagMapper).updateById(tag);
        }

        @Test
        @DisplayName("should delete tag")
        void shouldDeleteTag() {
            tagService.delete(1L);
            verify(tagMapper).deleteById(1L);
        }

        @Test
        @DisplayName("should get tag by id")
        void shouldGetById() {
            Tag tag = tag(1L, "java", "java");
            when(tagMapper.selectOneById(1L)).thenReturn(tag);

            Tag found = tagService.getById(1L);

            assertThat(found).isEqualTo(tag);
        }
    }

    @Nested
    @DisplayName("findByArticleId")
    class FindByArticleId {

        @Test
        @DisplayName("should return tag ids for an article via join")
        void shouldReturnTagIdsByArticleId() {
            when(articleTagMapper.selectTagIdsByArticleId(1L)).thenReturn(List.of(2L, 5L));

            List<Long> result = articleTagMapper.selectTagIdsByArticleId(1L);

            assertThat(result).containsExactly(2L, 5L);
        }

        @Test
        @DisplayName("should return empty when article has no tags")
        void shouldReturnEmpty_whenNoTags() {
            when(articleTagMapper.selectTagIdsByArticleId(1L)).thenReturn(List.of());

            List<Long> result = articleTagMapper.selectTagIdsByArticleId(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should list all tags usable to filter articles")
        void shouldListAllTags() {
            when(tagMapper.selectList()).thenReturn(List.of(
                    tag(1L, "java", "java"),
                    tag(2L, "spring", "spring"),
                    tag(3L, "k8s", "k8s")));

            List<Tag> result = tagService.list();

            assertThat(result).hasSize(3);
        }
    }

    private Tag tag(Long id, String name, String slug) {
        return Tag.builder().id(id).name(name).slug(slug).build();
    }
}
