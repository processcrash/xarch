package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Dept;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeptService unit tests
 */
@XarchTestBase
@DisplayName("DeptService Unit Tests")
class DeptServiceTest {

    private final DeptService deptService;

    DeptServiceTest(DeptService deptService) {
        this.deptService = deptService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<Dept> result = deptService.page(null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with name filter")
    void testPageWithName() {
        PageResult<Dept> result = deptService.page("tech", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("List returns departments")
    void testList() {
        List<Dept> depts = deptService.list();
        assertNotNull(depts);
    }

    @Test
    @DisplayName("Tree returns hierarchical structure")
    void testTree() {
        List<Dept> tree = deptService.tree();
        assertNotNull(tree);
    }

    @Test
    @DisplayName("Get by ID")
    void testGetById() {
        Dept dept = deptService.getById(1L);
        if (dept != null) {
            assertNotNull(dept.getDeptName());
        }
    }
}
