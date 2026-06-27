package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.Menu;
import com.xarch.example.system.service.MenuService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub MenuService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    @Override public PageResult<Menu> page(String n, int p, int s) { return PageResult.empty(); }
    @Override public List<Menu> tree() { return List.of(); }
    @Override public Menu getById(Long id) { return null; }
    @Override public void create(Menu menu) { }
    @Override public void update(Menu menu) { }
    @Override public void delete(Long id) { }
}