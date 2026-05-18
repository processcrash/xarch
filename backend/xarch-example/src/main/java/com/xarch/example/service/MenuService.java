package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Menu;
import com.xarch.example.mapper.MenuMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Menu service
 */
@Service
public class MenuService {

    @Autowired
    private MenuMapper menuMapper;

    public PageResult<Menu> page(String menuName, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Menu>();
        if (menuName != null && !menuName.isEmpty()) {
            wrapper.like(Menu::getMenuName, menuName);
        }
        wrapper.orderByAsc(Menu::getSortOrder);

        Page<Menu> page = new Page<>(pageNum, pageSize);
        Page<Menu> result = menuMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }

    public List<Menu> list() {
        return menuMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Menu>()
                .orderByAsc(Menu::getSortOrder)
        );
    }

    public List<Menu> tree() {
        List<Menu> allMenus = list();
        return buildTree(0L, allMenus);
    }

    private List<Menu> buildTree(Long parentId, List<Menu> allMenus) {
        return allMenus.stream()
            .filter(m -> m.getParentId().equals(parentId))
            .peek(m -> m.setChildren(buildTree(m.getId(), allMenus)))
            .toList();
    }

    public void create(Menu menu) {
        menuMapper.insert(menu);
    }

    public void update(Menu menu) {
        menuMapper.updateById(menu);
    }

    public void delete(Long id) {
        menuMapper.deleteById(id);
    }
}