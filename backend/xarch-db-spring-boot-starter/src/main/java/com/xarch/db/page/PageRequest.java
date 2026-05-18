package com.xarch.db.page;

import java.io.Serializable;

/**
 * Page request wrapper
 */
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private int pageNum = 1;
    private int pageSize = 10;
    private String orderBy;
    private String sortDirection = "asc";

    public PageRequest() {
    }

    public PageRequest(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}