package com.xarch.example.integration;

import com.xarch.example.XarchIntegrationTestBase;
import com.xarch.example.entity.Dept;
import com.xarch.example.service.DeptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Department Tree Integration Test
 * Tests building and navigating the department tree structure
 */
@XarchIntegrationTestBase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Dept Tree Integration Test")
class DeptTreeIntegrationTest {

    @Autowired
    private DeptService deptService;

    private static Long parentId;

    @Test
    @Order(1)
    @DisplayName("Create parent department")
    void testCreateParent() {
        Dept parent = new Dept();
        parent.setParentId(0L);
        parent.setDeptName("Parent Dept");
        parent.setDeptCode("PARENT_" + System.currentTimeMillis());
        parent.setSortOrder(1);
        parent.setStatus(1);
        deptService.create(parent);
        parentId = parent.getId();
        assertNotNull(parentId);
    }

    @Test
    @Order(2)
    @DisplayName("Create child department")
    void testCreateChild() {
        Dept child = new Dept();
        child.setParentId(parentId);
        child.setDeptName("Child Dept");
        child.setDeptCode("CHILD_" + System.currentTimeMillis());
        child.setSortOrder(1);
        child.setStatus(1);
        deptService.create(child);
        assertNotNull(child.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Build department tree")
    void testBuildTree() {
        List<Dept> tree = deptService.tree();
        assertNotNull(tree);

        // Find our parent in the tree
        boolean found = tree.stream()
                .anyMatch(d -> d.getId().equals(parentId)
                        && d.getChildren() != null
                        && d.getChildren().size() > 0);
        assertTrue(found, "Parent should have children in tree");
    }

    @Test
    @Order(4)
    @DisplayName("List all departments")
    void testList() {
        List<Dept> list = deptService.list();
        assertNotNull(list);
        assertTrue(list.size() >= 2);
    }
}
