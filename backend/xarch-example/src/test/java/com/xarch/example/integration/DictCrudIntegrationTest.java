package com.xarch.example.integration;

import com.xarch.example.XarchIntegrationTestBase;
import com.xarch.example.entity.Dict;
import com.xarch.example.entity.DictData;
import com.xarch.example.mapper.DictMapper;
import com.xarch.example.mapper.DictDataMapper;
import com.xarch.example.service.DictService;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dict CRUD Integration Test
 * Tests complete CRUD flow with real database (H2 in-memory)
 */
@XarchIntegrationTestBase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Dict CRUD Integration Test")
class DictCrudIntegrationTest {

    @Autowired
    private DictService dictService;

    @Autowired
    private DictMapper dictMapper;

    @Autowired
    private DictDataMapper dictDataMapper;

    private static Long testDictId;

    @Test
    @Order(1)
    @DisplayName("Create dictionary")
    void testCreate() {
        Dict dict = new Dict();
        dict.setDictName("Integration Test Dict");
        dict.setDictCode("INTEG_TEST_" + System.currentTimeMillis());
        dict.setDescription("Test description");
        dict.setStatus(1);

        dictService.create(dict);
        assertNotNull(dict.getId());
        testDictId = dict.getId();
        assertTrue(testDictId > 0);
    }

    @Test
    @Order(2)
    @DisplayName("Get created dictionary")
    void testGetById() {
        Dict dict = dictService.getById(testDictId);
        assertNotNull(dict);
        assertEquals("Integration Test Dict", dict.getDictName());
    }

    @Test
    @Order(3)
    @DisplayName("Update dictionary")
    void testUpdate() {
        Dict dict = dictService.getById(testDictId);
        dict.setDictName("Updated Name");
        dictService.update(dict);

        Dict updated = dictService.getById(testDictId);
        assertEquals("Updated Name", updated.getDictName());
    }

    @Test
    @Order(4)
    @DisplayName("Add dict data")
    void testAddDictData() {
        DictData data = new DictData();
        data.setDictId(testDictId);
        data.setDictLabel("Test Label");
        data.setDictValue("test_value");
        data.setStatus(1);

        dictService.createData(data);
        assertNotNull(data.getId());

        List<DictData> dataList = dictService.getDataByDictId(testDictId);
        assertTrue(dataList.size() > 0);
    }

    @Test
    @Order(5)
    @DisplayName("Page query dictionaries")
    void testPage() {
        PageResult<Dict> result = dictService.page(null, 1, 10);
        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
    }

    @Test
    @Order(6)
    @DisplayName("Delete dictionary")
    void testDelete() {
        dictService.delete(testDictId);
        // After delete (soft), list should not include it
        List<Dict> list = dictService.list();
        assertTrue(list.stream().noneMatch(d -> d.getId().equals(testDictId) && d.getDelFlag() == 0));
    }
}
