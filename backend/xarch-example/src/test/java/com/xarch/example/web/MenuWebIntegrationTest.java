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
 * Menu Web layer integration tests
 */
@XarchTestBase
@DisplayName("Menu Web Integration Tests")
class MenuWebIntegrationTest {

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
    @DisplayName("GET /api/menus - list all")
    void testList() throws Exception {
        mockMvc().perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("GET /api/menus/page - page query")
    void testPage() throws Exception {
        mockMvc().perform(get("/api/menus/page")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/menus/tree - tree structure")
    void testTree() throws Exception {
        mockMvc().perform(get("/api/menus/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("GET /api/menus/treeselect - tree for transfer")
    void testTreeSelect() throws Exception {
        mockMvc().perform(get("/api/menus/treeselect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }
}
