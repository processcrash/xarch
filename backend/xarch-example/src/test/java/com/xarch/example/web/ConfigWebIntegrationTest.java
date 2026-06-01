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
 * Config Web layer integration tests
 */
@XarchTestBase
@DisplayName("Config Web Integration Tests")
class ConfigWebIntegrationTest {

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
    @DisplayName("GET /api/configs - page query")
    void testPage() throws Exception {
        mockMvc().perform(get("/api/configs")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/configs/{id} - get detail")
    void testDetail() throws Exception {
        mockMvc().perform(get("/api/configs/1"))
                .andExpect(status().isOk());
    }
}
