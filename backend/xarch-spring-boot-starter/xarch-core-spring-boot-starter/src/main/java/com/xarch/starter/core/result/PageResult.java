package com.xarch.starter.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Page result wrapper
 */
@Data
@Schema(description = "Page result")
public class PageResult<T> implements Serializable {

    @Schema(description = "Total count")
    private long total;

    @Schema(description = "Current page data")
    private List<T> list;

    public PageResult() {
    }

    public PageResult(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }

    public static <T> PageResult<T> of(List<T> list, long total) {
        return new PageResult<>(list, total);
    }

    public static <T> PageResult<T> ok(List<T> list) {
        return new PageResult<>(list, list == null ? 0 : list.size());
    }
}