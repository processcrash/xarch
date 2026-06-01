package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Menu;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MenuService unit tests
 */
@XarchTestBase
@DisplayName("MenuService Unit Tests")
class MenuServiceTest {

    private final MenuService menuService;

    MenuServiceTest(MenuService menuService) {
        this.menuService = menuService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<Menu> result = menuService.page(null, 1, 10);
        assertNotNull(result);
        assertNotNull(result.getList());
    }

    @Test
    @DisplayName("Page query with name filter")
    void testPageWithName() {
        PageResult<Menu> result = menuService.page("user", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("List returns menus")
    void testList() {
        List<Menu> menus = menuService.list();
        assertNotNull(menus);
    }

    @Test
    @DisplayName("Tree returns hierarchical structure")
    void testTree() {
        List<Menu> tree = menuService.tree();
        assertNotNull(tree);
    }

    @Test
    @DisplayName("Get by ID")
    void testGetById() {
        Menu menu = menuService.getById(1L);
        if (menu != null) {
            assertNotNull(menu.getMenuName());
        }
    }
}
