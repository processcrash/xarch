package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Dict;
import com.xarch.example.entity.DictData;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DictService unit tests
 */
@XarchTestBase
@DisplayName("DictService Unit Tests")
class DictServiceTest {

    private final DictService dictService;

    DictServiceTest(DictService dictService) {
        this.dictService = dictService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<Dict> result = dictService.page(null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with name filter")
    void testPageWithName() {
        PageResult<Dict> result = dictService.page("user", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("List returns dictionaries")
    void testList() {
        List<Dict> dicts = dictService.list();
        assertNotNull(dicts);
    }

    @Test
    @DisplayName("Get by ID")
    void testGetById() {
        Dict dict = dictService.getById(1L);
        if (dict != null) {
            assertNotNull(dict.getDictName());
        }
    }

    @Test
    @DisplayName("Get data by dict code returns list")
    void testGetDataByDictCode() {
        List<DictData> data = dictService.getDataByDictCode("non_existent");
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }
}
