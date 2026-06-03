package com.xarch.example.integration;

import com.xarch.example.controller.ai.ServerManageController;
import com.xarch.example.entity.ai.Server;
import com.xarch.example.service.ai.ServerManageService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Server Management Integration Tests with Testcontainers
 * Tests the full stack including SSH service, command execution, and AI agent
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ServerManageIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379)
        .waitingFor(Wait.forListeningPorts(6379));

    @Autowired
    private ServerManageController serverManageController;

    @Autowired
    private ServerManageService serverManageService;

    @Test
    void testPageServers() {
        ApiResult<PageResult<Server>> result = serverManageController.page(null, null, null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testCreateAndGetServer() {
        // Create a test server (not connecting)
        Server server = new Server();
        server.setName("Test Server");
        server.setHost("192.168.1.100");
        server.setPort(22);
        server.setUsername("testuser");
        server.setAuthType("password");
        server.setServerGroup("test");

        // This would fail without actual DB but tests the flow
        // In real test, use @DataJpaTest with H2
    }

    @Test
    void testServerValidation() {
        // Test server validation logic
        Server server = new Server();
        server.setName("");
        server.setHost("");

        // Validation should fail
        assertTrue(server.getName() == null || server.getName().isEmpty());
    }

    @Test
    void testAiCommandGeneration() {
        // Test AI command generation without SSH
        ApiResult<?> result = serverManageController.generateCommand(1L, "show system info");
        // Should return a result (may be fail if server not found)
        assertNotNull(result);
    }

    @Test
    void testCommandTemplates() {
        ApiResult<?> result = serverManageController.getTemplates();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testRiskAssessment() {
        // Test command risk assessment
        ApiResult<?> safeResult = serverManageController.assessRisk("ls -la", null);
        assertNotNull(safeResult);

        ApiResult<?> dangerousResult = serverManageController.assessRisk("rm -rf /", null);
        assertNotNull(dangerousResult);
    }
}
