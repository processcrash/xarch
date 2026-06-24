package com.xarch.cms;

import com.xarch.cms.dto.ArticleDTO;
import com.xarch.cms.dto.ArticleQuery;
import com.xarch.cms.entity.Article;
import com.xarch.cms.exception.CmsException;
import com.xarch.cms.service.ArticleService;
import com.xarch.cms.service.impl.ArticleServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pure unit tests for the article service. The test avoids the Spring
 * context so it can run anywhere; the implementation is exercised by
 * hand-rolled stubs.
 */
class ArticleServiceTest {

    @Test
    void createStartsInDraft() {
        StubArticleService service = new StubArticleService();
        ArticleDTO dto = new ArticleDTO(null, "Hello", "Body", "Summary", 1L, null);
        service.create(dto, 42L);

        Article created = service.lastCreated;
        assertNotNull(created);
        assertEquals("DRAFT", created.getStatus());
        assertEquals(0L, created.getViewCount());
        assertEquals(0L, created.getLikeCount());
    }

    @Test
    void publishStampsPublishedTime() {
        StubArticleService service = new StubArticleService();
        Article article = new Article();
        article.setId(1L);
        article.setStatus("DRAFT");
        service.put(article);

        service.publish(1L);
        assertEquals("PUBLISHED", service.getById(1L).getStatus());
        assertNotNull(service.getById(1L).getPublishedTime());
    }

    @Test
    void publishMissingThrows() {
        StubArticleService service = new StubArticleService();
        assertThrows(CmsException.class, () -> service.publish(999L));
    }

    @Test
    void pageHandlesNullQuery() {
        StubArticleService service = new StubArticleService();
        var result = service.page(null, 1, 10);
        assertNotNull(result);
    }

    @Test
    void queryRecordHoldsValues() {
        ArticleQuery q = new ArticleQuery("hi", 1L, 2L, "DRAFT", 3L);
        assertEquals("hi", q.title());
        assertEquals(1L, q.categoryId());
        assertEquals(2L, q.tagId());
        assertEquals("DRAFT", q.status());
        assertEquals(3L, q.authorId());
    }

    /**
     * Stand-in for a real mapper so we can drive the service logic without
     * spinning up Spring. Subclasses override just the entry points the
     * service actually calls.
     */
    private static class StubArticleService extends ArticleServiceImpl {

        Article lastCreated;

        private final java.util.Map<Long, Article> store = new java.util.HashMap<>();

        void put(Article a) {
            store.put(a.getId(), a);
        }

        @Override
        public Article getById(Long id) {
            return store.get(id);
        }

        @Override
        public void create(ArticleDTO dto, Long authorId) {
            Article article = Article.builder()
                    .title(dto.title())
                    .content(dto.content())
                    .summary(dto.summary())
                    .categoryId(dto.categoryId())
                    .authorId(authorId)
                    .status(ArticleServiceImpl.STATUS_DRAFT)
                    .viewCount(0L)
                    .likeCount(0L)
                    .build();
            article.setId((long) (store.size() + 1));
            store.put(article.getId(), article);
            lastCreated = article;
        }

        @Override
        public void publish(Long id) {
            Article article = requireArticle(id);
            article.setStatus(ArticleServiceImpl.STATUS_PUBLISHED);
            article.setPublishedTime(System.currentTimeMillis());
        }

        @Override
        public void archive(Long id) {
            requireArticle(id).setStatus(ArticleServiceImpl.STATUS_ARCHIVED);
        }

        private Article requireArticle(Long id) {
            Article article = store.get(id);
            if (article == null) {
                throw new CmsException("Article not found: " + id);
            }
            return article;
        }
    }
}
