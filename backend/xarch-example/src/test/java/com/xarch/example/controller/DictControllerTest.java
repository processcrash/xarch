package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DictController unit tests
 */
@SpringBootTest
class DictControllerTest {

    @Autowired
    private DictController dictController;

    @Test
    void testPage() {
        ApiResult<PageResult<com.xarch.example.entity.Dict>> result = dictController.page(null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetDataByCode() {
        ApiResult<List<com.xarch.example.entity.DictData>> result = dictController.getDataByCode("user_status");
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}