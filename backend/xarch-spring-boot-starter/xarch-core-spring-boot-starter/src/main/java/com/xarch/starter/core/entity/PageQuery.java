package com.xarch.starter.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Page request
 */
@Data
@Schema(description = "Page request")
public class PageQuery implements Serializable {

    @Schema(description = "Page number", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "Page size", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "Sort field")
    private String orderBy;

    @Schema(description = "Sort direction: asc, desc")
    private String orderDirection = "desc";

    public Integer getPageNum() {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    public Integer getPageSize() {
        return pageSize == null || pageSize < 1 ? 10 : pageSize > 100 ? 100 : pageSize;
    }
}