package com.xarch.example.entity;

/**
 * 缓存信息实体
 */
public class SysCache {
    /** 缓存名称 */
    private String name;

    /** 缓存键名 */
    private String key;

    /** 缓存内容 */
    private String description;

    public SysCache() {
    }

    public SysCache(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public SysCache(String name, String key, String description) {
        this.name = name;
        this.key = key;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}