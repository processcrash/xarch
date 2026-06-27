package com.xarch.example.monitor.entity;

/** 缓存信息实体 used by the cache monitor. */
public class SysCache {
    private String name;
    private String key;
    private String description;

    public SysCache() {}

    public SysCache(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public SysCache(String name, String key, String description) {
        this.name = name;
        this.key = key;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}