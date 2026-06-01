package com.xarch.example.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.User;
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
 * User Web layer integration tests
 * Tests HTTP endpoints through MockMvc
 */
@XarchTestBase
@DisplayName("User Web Integration Tests")
class UserWebIntegrationTest {

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
    @DisplayName("GET /api/users - page query")
    void testPage() throws Exception {
        mockMvc().perform(get("/api/users")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("GET /api/users with username filter")
    void testPageWithUsername() throws Exception {
        mockMvc().perform(get("/api/users")
                .param("username", "admin")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("GET /api/users/options - list options")
    void testOptions() throws Exception {
        mockMvc().perform(get("/api/users/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - get detail")
    void testDetail() throws Exception {
        mockMvc().perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("POST /api/users - create user")
    void testCreate() throws Exception {
        User user = new User();
        user.setUsername("test_user_" + System.currentTimeMillis());
        user.setPassword("password123");
        user.setNickname("Test User");
        user.setEmail("test@example.com");
        user.setStatus(1);

        mockMvc().perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - update user")
    void testUpdate() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setNickname("Updated Nickname");
        user.setStatus(1);

        mockMvc().perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - delete user")
    void testDelete() throws Exception {
        mockMvc().perform(delete("/api/users/999999"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/{id}/roles - get user roles")
    void testGetUserRoles() throws Exception {
        mockMvc().perform(get("/api/users/1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }
}
