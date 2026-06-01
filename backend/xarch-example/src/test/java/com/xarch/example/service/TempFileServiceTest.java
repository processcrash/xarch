package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.TempFile;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TempFileService unit tests
 */
@XarchTestBase
@DisplayName("TempFileService Unit Tests")
class TempFileServiceTest {

    private final TempFileService tempFileService;

    TempFileServiceTest(TempFileService tempFileService) {
        this.tempFileService = tempFileService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<TempFile> result = tempFileService.page(null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with name filter")
    void testPageWithName() {
        PageResult<TempFile> result = tempFileService.page("test", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get by ID")
    void testGetById() {
        TempFile file = tempFileService.getById(1L);
        if (file != null) {
            assertNotNull(file.getFileName());
        }
    }
}
