package com.xarch.cms;

import com.mybatisflex.core.paginate.Page;
import com.xarch.cms.dto.ArticleDTO;
import com.xarch.cms.dto.ArticleQuery;
import com.xarch.cms.entity.Article;
import com.xarch.cms.exception.CmsException;
import com.xarch.cms.mapper.ArticleMapper;
import com.xarch.cms.mapper.ArticleTagMapper;
import com.xarch.cms.service.impl.ArticleServiceImpl;
import com.xarch.starter.core.result.PageResult;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ArticleServiceImpl}. Uses Mockito to stub mapper
 * interactions and AssertJ for assertions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Article Service Tests")
class ArticleServiceImplTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private ArticleTagMapper articleTagMapper;

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return article when it exists")
        void shouldReturnArticle_whenExists() {
            Article article = sampleArticle(1L);
            when(articleMapper.selectOneById(1L)).thenReturn(article);

            Article found = articleService.getById(1L);

            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(1L);
            assertThat(found.getTitle()).isEqualTo("Title");
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNull_whenNotFound() {
            when(articleMapper.selectOneById(99L)).thenReturn(null);

            Article found = articleService.getById(99L);

            assertThat(found).isNull();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should set defaults and DRAFT status")
        void shouldSetDefaults_whenCreating() {
            ArticleDTO dto = new ArticleDTO(null, "Hello", "Body", "Summary", 1L, null);
            when(articleMapper.insert(any(Article.class))).thenAnswer(inv -> {
                Article a = inv.getArgument(0);
                a.setId(10L);
                return 1;
            });

            articleService.create(dto, 42L);

            ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
            verify(articleMapper).insert(captor.capture());
            Article created = captor.getValue();
            assertThat(created.getTitle()).isEqualTo("Hello");
            assertThat(created.getContent()).isEqualTo("Body");
            assertThat(created.getSummary()).isEqualTo("Summary");
            assertThat(created.getCategoryId()).isEqualTo(1L);
            assertThat(created.getAuthorId()).isEqualTo(42L);
            assertThat(created.getStatus()).isEqualTo(ArticleServiceImpl.STATUS_DRAFT);
            assertThat(created.getViewCount()).isEqualTo(0L);
            assertThat(created.getLikeCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("should attach tags when tag ids provided")
        void shouldAttachTags_whenTagIdsProvided() {
            ArticleDTO dto = new ArticleDTO(null, "Hello", "Body", "Summary", 1L, List.of(7L, 8L));
            when(articleMapper.insert(any(Article.class))).thenAnswer(inv -> {
                Article a = inv.getArgument(0);
                a.setId(11L);
                return 1;
            });

            articleService.create(dto, 42L);

            verify(articleTagMapper, org.mockito.Mockito.times(2))
                    .insert(any(com.xarch.cms.entity.ArticleTag.class));
        }

        @Test
        @DisplayName("should not attach tags when no tag ids")
        void shouldNotAttachTags_whenNoTagIds() {
            ArticleDTO dto = new ArticleDTO(null, "Hello", "Body", "Summary", 1L, null);
            when(articleMapper.insert(any(Article.class))).thenAnswer(inv -> {
                Article a = inv.getArgument(0);
                a.setId(12L);
                return 1;
            });

            articleService.create(dto, 42L);

            verify(articleTagMapper, never()).insert(any(com.xarch.cms.entity.ArticleTag.class));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update fields when article exists")
        void shouldUpdateFields_whenExists() {
            Article article = sampleArticle(1L);
            when(articleMapper.selectOneById(1L)).thenReturn(article);
            ArticleDTO dto = new ArticleDTO(null, "Updated", "New", "Sum", 2L, null);

            articleService.update(1L, dto);

            assertThat(article.getTitle()).isEqualTo("Updated");
            assertThat(article.getContent()).isEqualTo("New");
            assertThat(article.getSummary()).isEqualTo("Sum");
            assertThat(article.getCategoryId()).isEqualTo(2L);
            verify(articleMapper).update(article);
        }

        @Test
        @DisplayName("should throw when article not found")
        void shouldThrow_whenNotFound() {
            when(articleMapper.selectOneById(1L)).thenReturn(null);
            ArticleDTO dto = new ArticleDTO(null, "x", "y", null, 1L, null);

            assertThatThrownBy(() -> articleService.update(1L, dto))
                    .isInstanceOf(CmsException.class)
                    .hasMessageContaining("Article not found");
        }
    }

    @Nested
    @DisplayName("publish / archive")
    class Lifecycle {

        @Test
        @DisplayName("should move to PUBLISHED and stamp publishedTime")
        void shouldPublishArticle() {
            Article article = sampleArticle(1L);
            article.setStatus(ArticleServiceImpl.STATUS_DRAFT);
            when(articleMapper.selectOneById(1L)).thenReturn(article);

            articleService.publish(1L);

            assertThat(article.getStatus()).isEqualTo(ArticleServiceImpl.STATUS_PUBLISHED);
            assertThat(article.getPublishedTime()).isNotNull();
            verify(articleMapper).update(article);
        }

        @Test
        @DisplayName("should move to ARCHIVED")
        void shouldArchiveArticle() {
            Article article = sampleArticle(1L);
            when(articleMapper.selectOneById(1L)).thenReturn(article);

            articleService.archive(1L);

            assertThat(article.getStatus()).isEqualTo(ArticleServiceImpl.STATUS_ARCHIVED);
        }

        @Test
        @DisplayName("should throw on publish when article missing")
        void shouldThrowOnPublish_whenMissing() {
            when(articleMapper.selectOneById(1L)).thenReturn(null);

            assertThatThrownBy(() -> articleService.publish(1L))
                    .isInstanceOf(CmsException.class);
        }
    }

    @Nested
    @DisplayName("delete / view / like")
    class Counters {

        @Test
        @DisplayName("should delete article by id")
        void shouldDeleteArticle() {
            articleService.delete(1L);
            verify(articleMapper).deleteById(1L);
        }

        @Test
        @DisplayName("should increment view count and return new value")
        void shouldIncrementViewCount() {
            Article article = sampleArticle(1L);
            article.setViewCount(5L);
            when(articleMapper.selectOneById(1L)).thenReturn(article);

            long views = articleService.view(1L);

            verify(articleMapper).incrementViewCount(1L);
            assertThat(views).isEqualTo(5L);
        }

        @Test
        @DisplayName("should increment like count and return new value")
        void shouldIncrementLikeCount() {
            Article article = sampleArticle(1L);
            article.setLikeCount(7L);
            when(articleMapper.selectOneById(1L)).thenReturn(article);

            long likes = articleService.like(1L);

            verify(articleMapper).incrementLikeCount(1L);
            assertThat(likes).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("page")
    class PageTests {

        @Test
        @DisplayName("should accept null query and return empty page")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void shouldAcceptNullQuery() {
            Page page = Page.of(1, 10);
            page.setRecords(List.of());
            page.setTotalRow(0L);
            when(articleMapper.paginate(anyLong(), anyLong(), any(com.mybatisflex.core.query.QueryWrapper.class)))
                    .thenReturn(page);

            PageResult<Article> result = articleService.page(null, 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getList()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0L);
        }

        @Test
        @DisplayName("should pass query filters to mapper")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void shouldApplyQueryFilters() {
            Page page = Page.of(1, 10);
            page.setRecords(List.of(sampleArticle(1L)));
            page.setTotalRow(1L);
            when(articleMapper.paginate(anyLong(), anyLong(), any(com.mybatisflex.core.query.QueryWrapper.class)))
                    .thenReturn(page);

            ArticleQuery query = new ArticleQuery("hello", 1L, null, "DRAFT", 2L);
            PageResult<Article> result = articleService.page(query, 1, 10);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getTotal()).isEqualTo(1L);
            verify(articleMapper).paginate(1, 10, any(com.mybatisflex.core.query.QueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("listByTag / listByCategory")
    class ListJoins {

        @Test
        @DisplayName("should return articles by tag")
        void shouldListByTag() {
            when(articleMapper.selectByTagId(2L)).thenReturn(List.of(sampleArticle(1L)));

            List<Article> result = articleService.listByTag(2L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return articles by category")
        void shouldListByCategory() {
            when(articleMapper.selectByCategoryId(3L)).thenReturn(List.of(sampleArticle(1L)));

            List<Article> result = articleService.listByCategory(3L);

            assertThat(result).hasSize(1);
        }
    }

    private Article sampleArticle(long id) {
        return Article.builder()
                .id(id)
                .title("Title")
                .content("Content")
                .summary("Summary")
                .categoryId(1L)
                .authorId(42L)
                .status(ArticleServiceImpl.STATUS_DRAFT)
                .viewCount(0L)
                .likeCount(0L)
                .build();
    }
}
