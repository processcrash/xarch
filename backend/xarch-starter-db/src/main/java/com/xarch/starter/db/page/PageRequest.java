package com.xarch.starter.db.page;

import com.xarch.starter.core.entity.PageQuery;
import lombok.Data;

/**
 * Page request with query parameters
 */
@Data
public class PageRequest<T> extends PageQuery {

    private T params;
}