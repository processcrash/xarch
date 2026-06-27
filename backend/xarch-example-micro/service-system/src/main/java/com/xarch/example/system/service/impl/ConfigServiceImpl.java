package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.Config;
import com.xarch.example.system.service.ConfigService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Stub ConfigService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    @Override public PageResult<Config> page(String k, int p, int s) { return PageResult.empty(); }
    @Override public Config getById(Long id) { return null; }
    @Override public String getValue(String key) { return null; }
    @Override public void create(Config c) { }
    @Override public void update(Config c) { }
    @Override public void delete(Long id) { }
}