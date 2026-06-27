package com.xarch.example.system.service;

import com.xarch.example.system.entity.Menu;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/** Menu service contract. */
public interface MenuService {
    PageResult<Menu> page(String menuName, int pageNum, int pageSize);
    List<Menu> tree();
    Menu getById(Long id);
    void create(Menu menu);
    void update(Menu menu);
    void delete(Long id);
}