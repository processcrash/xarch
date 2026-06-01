package com.xarch.example.web;

import com.xarch.example.XarchTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dict Web layer integration tests
 */
@XarchTestBase
@DisplayName("Dict Web Integration Tests")
class DictWebIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private MockMvc mockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
        return mockMvc;
    }

    @Test
    @DisplayName("GET /api/dicts - page query")
    void testPage() throws Exception {
        mockMvc().perform(get("/api/dicts")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/dicts/list - list all")
    void testList() throws Exception {
        mockMvc().perform(get("/api/dicts/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("GET /api/dicts/data/{dictCode} - get dict data by code")
    void testGetDataByCode() throws Exception {
        mockMvc().perform(get("/api/dicts/data/sys_user_sex"))
                .andExpect(status().isOk());
    }
}
