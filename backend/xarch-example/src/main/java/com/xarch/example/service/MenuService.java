package com.xarch.example.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.Menu;
import com.xarch.example.mapper.MenuMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Menu service
 */
@Service
public class MenuService {

    @Autowired
    private MenuMapper menuMapper;

    public PageResult<Menu> page(String menuName, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_menu").where("del_flag = 0");
        if (StringUtils.hasText(menuName)) {
            wrapper.and("menu_name LIKE ?", "%" + menuName + "%");
        }
        wrapper.orderBy("sort_order", true);

        Page<Menu> page = menuMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }

    public List<Menu> list() {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_menu")
                .where("del_flag = 0")
                .orderBy("sort_order", true);
        return menuMapper.selectListByQuery(wrapper);
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