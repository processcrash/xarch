package com.xarch.cms.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.cms.dto.ArticleDTO;
import com.xarch.cms.dto.ArticleQuery;
import com.xarch.cms.entity.Article;
import com.xarch.cms.entity.ArticleTag;
import com.xarch.cms.exception.CmsException;
import com.xarch.cms.mapper.ArticleMapper;
import com.xarch.cms.mapper.ArticleTagMapper;
import com.xarch.cms.service.ArticleService;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Article service implementation.
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    public PageResult<Article> page(ArticleQuery query, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from(Article.class);
        if (query != null) {
            if (StringUtils.hasText(query.title())) {
                wrapper.and("title LIKE ?", "%" + query.title() + "%");
            }
            if (query.categoryId() != null) {
                wrapper.and("category_id = ?", query.categoryId());
            }
            if (query.authorId() != null) {
                wrapper.and("author_id = ?", query.authorId());
            }
            if (StringUtils.hasText(query.status())) {
                wrapper.and("status = ?", query.status());
            }
        }
        wrapper.orderBy("create_time", false);
        Page<Article> page = articleMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    @Override
    public Article getById(Long id) {
        return articleMapper.selectOneById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ArticleDTO dto, Long authorId) {
        Article article = Article.builder()
                .title(dto.title())
                .content(dto.content())
                .summary(dto.summary())
                .categoryId(dto.categoryId())
                .authorId(authorId)
                .status(STATUS_DRAFT)
                .viewCount(0L)
                .likeCount(0L)
                .build();
        articleMapper.insert(article);
        attachTags(article.getId(), dto.tagIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ArticleDTO dto) {
        Article article = requireArticle(id);
        article.setTitle(dto.title());
        article.setContent(dto.content());
        article.setSummary(dto.summary());
        article.setCategoryId(dto.categoryId());
        articleMapper.update(article);
        if (dto.tagIds() != null) {
            articleTagMapper.deleteByQuery(QueryWrapper.create()
                    .from(ArticleTag.class)
                    .where("article_id = ?", id));
            attachTags(id, dto.tagIds());
        }
    }

    @Override
    public void publish(Long id) {
        Article article = requireArticle(id);
        article.setStatus(STATUS_PUBLISHED);
        article.setPublishedTime(System.currentTimeMillis());
        articleMapper.update(article);
    }

    @Override
    public void archive(Long id) {
        Article article = requireArticle(id);
        article.setStatus(STATUS_ARCHIVED);
        articleMapper.update(article);
    }

    @Override
    public void delete(Long id) {
        articleMapper.deleteById(id);
    }

    @Override
    public long view(Long id) {
        articleMapper.incrementViewCount(id);
        Article article = articleMapper.selectOneById(id);
        return Objects.requireNonNullElse(article, new Article()).getViewCount();
    }

    @Override
    public long like(Long id) {
        articleMapper.incrementLikeCount(id);
        Article article = articleMapper.selectOneById(id);
        return Objects.requireNonNullElse(article, new Article()).getLikeCount();
    }

    @Override
    public List<Article> listByTag(Long tagId) {
        return articleMapper.selectByTagId(tagId);
    }

    @Override
    public List<Article> listByCategory(Long categoryId) {
        return articleMapper.selectByCategoryId(categoryId);
    }

    /**
     * Re-link an article to the given tag ids.
     */
    private void attachTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            articleTagMapper.insert(ArticleTag.builder()
                    .articleId(articleId)
                    .tagId(tagId)
                    .build());
        }
    }

    /**
     * Look up an article or throw a domain exception.
     */
    private Article requireArticle(Long id) {
        Article article = articleMapper.selectOneById(id);
        if (article == null) {
            throw new CmsException("Article not found: " + id);
        }
        return article;
    }
}
