package com.xarch.example.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Role Web layer integration tests
 */
@XarchTestBase
@DisplayName("Role Web Integration Tests")
class RoleWebIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private MockMvc mockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
        return mockMvc;
    }

    @Test
    @DisplayName("GET /api/roles - page query")
    void testPage() throws Exception {
        mockMvc().perform(get("/api/roles")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("GET /api/roles/options - list options")
    void testOptions() throws Exception {
        mockMvc().perform(get("/api/roles/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("GET /api/roles/{id} - get detail")
    void testDetail() throws Exception {
        mockMvc().perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("POST /api/roles - create role")
    void testCreate() throws Exception {
        Role role = new Role();
        role.setRoleName("test_role_" + System.currentTimeMillis());
        role.setRoleCode("TEST_" + System.currentTimeMillis());
        role.setRoleType(2);
        role.setStatus(1);

        mockMvc().perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/roles/{id} - update role")
    void testUpdate() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("Updated Role");
        role.setStatus(1);

        mockMvc().perform(put("/api/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/roles/{id} - delete role")
    void testDelete() throws Exception {
        mockMvc().perform(delete("/api/roles/999999"))
                .andExpect(status().isOk());
    }
}
