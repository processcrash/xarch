package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.OpLog;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpLogService unit tests
 */
@XarchTestBase
@DisplayName("OpLogService Unit Tests")
class OpLogServiceTest {

    private final OpLogService opLogService;

    OpLogServiceTest(OpLogService opLogService) {
        this.opLogService = opLogService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<OpLog> result = opLogService.page(null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with username filter")
    void testPageWithUsername() {
        PageResult<OpLog> result = opLogService.page("admin", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Save operation log")
    void testSave() {
        OpLog log = new OpLog();
        log.setUsername("testuser");
        log.setOperation("Test");
        log.setType("QUERY");
        log.setStatus(1);

        assertDoesNotThrow(() -> opLogService.save(log));
    }
}
