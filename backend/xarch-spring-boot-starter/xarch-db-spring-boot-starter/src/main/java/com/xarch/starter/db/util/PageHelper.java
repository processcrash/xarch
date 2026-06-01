package com.xarch.starter.db.util;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Page helper for MybatisFlex
 */
public final class PageHelper {

    private PageHelper() {
    }

    /**
     * Build a Page object
     */
    public static <T> Page<T> buildPage(int pageNum, int pageSize) {
        return Page.of(pageNum, pageSize);
    }

    /**
     * Convert Page result to PageResult
     */
    public static <T> PageResult<T> toPageResult(Page<T> page) {
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    /**
     * Convert list with total to PageResult
     */
    public static <T> PageResult<T> toPageResult(List<T> list, long total) {
        return PageResult.of(list, total);
    }

    /**
     * Add LIKE condition if value is not blank
     */
    public static <T> QueryWrapper addLikeIfPresent(QueryWrapper wrapper, String column, String value) {
        if (StringUtils.hasText(value)) {
            return wrapper.like(column, value);
        }
        return wrapper;
    }

    /**
     * Add EQ condition if value is not blank
     */
    public static <T> QueryWrapper addEqIfPresent(QueryWrapper wrapper, String column, Object value) {
        if (value != null) {
            if (value instanceof String && !StringUtils.hasText((String) value)) {
                return wrapper;
            }
            return wrapper.eq(column, value);
        }
        return wrapper;
    }
}