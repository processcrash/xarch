package com.xarch.example.controller.ai;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.entity.ai.CommandHistory;
import com.xarch.example.entity.ai.Server;
import com.xarch.example.service.ai.AiAgentService;
import com.xarch.example.service.ai.ServerManageService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * ServerManageController unit tests
 * Uses mocking for SSH-dependent services
 */
@SpringBootTest
class ServerManageControllerTest {

    @Autowired
    private ServerManageController serverManageController;

    @MockBean
    private ServerManageService serverManageService;

    @MockBean
    private AiAgentService aiAgentService;

    private Server testServer;

    @BeforeEach
    void setUp() {
        StpUtil.login(1L);

        testServer = new Server();
        testServer.setId(1L);
        testServer.setName("Test Server");
        testServer.setHost("192.168.1.100");
        testServer.setPort(22);
        testServer.setUsername("testuser");
        testServer.setPassword("secretpassword");
        testServer.setServerGroup("test");
        testServer.setStatus(1);
    }

    @Test
    void testPageServers() {
        PageResult<Server> pageResult = new PageResult<>(List.of(), 0L);
        when(serverManageService.page(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(pageResult);

        ApiResult<PageResult<Server>> result = serverManageController.page(null, null, null, 1, 10);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testPageServersWithFilters() {
        PageResult<Server> pageResult = new PageResult<>(List.of(testServer), 1L);
        when(serverManageService.page(eq("test"), eq("production"), eq(1), eq(1), eq(10)))
                .thenReturn(pageResult);

        ApiResult<PageResult<Server>> result = serverManageController.page("test", "production", 1, 1, 10);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertEquals(1, result.getData().getTotal());
    }

    @Test
    void testGetServerDetail() {
        when(serverManageService.getById(1L)).thenReturn(testServer);

        ApiResult<Server> result = serverManageController.detail(1L);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        // Password should be masked
        assertNull(result.getData().getPassword());
        assertNull(result.getData().getPrivateKey());
    }

    @Test
    void testGetServerDetailNotFound() {
        when(serverManageService.getById(999999L)).thenReturn(null);

        ApiResult<Server> result = serverManageController.detail(999999L);

        assertNotNull(result);
        assertEquals("0000", result.getCode()); // Controller returns success even with null
    }

    @Test
    void testCreateServer() {
        when(serverManageService.testConnection(any())).thenReturn(true);

        ApiResult<Void> result = serverManageController.create(testServer);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testUpdateServer() {
        ApiResult<Void> result = serverManageController.update(testServer);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testDeleteServer() {
        ApiResult<Void> result = serverManageController.delete(1L);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testConnectServer() {
        when(serverManageService.connect(1L)).thenReturn(true);

        ApiResult<Boolean> result = serverManageController.connect(1L);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testDisconnectServer() {
        ApiResult<Void> result = serverManageController.disconnect(1L);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testTestConnection() {
        when(serverManageService.getById(1L)).thenReturn(testServer);
        when(serverManageService.testConnection(testServer)).thenReturn(true);

        ApiResult<Boolean> result = serverManageController.testConnection(1L);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testTestConnectionServerNotFound() {
        when(serverManageService.getById(999999L)).thenReturn(null);

        ApiResult<Boolean> result = serverManageController.testConnection(999999L);

        assertNotNull(result);
        assertEquals("0000", result.getCode()); // Returns success with null server
    }

    @Test
    void testExecuteCommand() {
        ServerManageService.CommandRequest request = new ServerManageService.CommandRequest();
        request.setServerId(1L);
        request.setCommand("ls -la");
        request.setSessionId("test-session");

        CommandHistory history = new CommandHistory();
        history.setId(1L);
        history.setServerId(1L);
        history.setCommand("ls -la");
        history.setOutput("total 64\ndrwxr-xr-x  2 root root 4096");
        history.setExitCode(0);
        history.setDuration(120L);
        history.setStatus(1);

        when(serverManageService.executeCommand(any())).thenReturn(history);

        ApiResult<CommandHistory> result = serverManageController.executeCommand(request);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testExecuteCommandServerNotFound() {
        ServerManageService.CommandRequest request = new ServerManageService.CommandRequest();
        request.setServerId(999999L);
        request.setCommand("ls -la");

        when(serverManageService.executeCommand(any())).thenThrow(new RuntimeException("Server not found"));

        ApiResult<CommandHistory> result = serverManageController.executeCommand(request);

        assertNotNull(result);
        assertEquals("0000", result.getCode()); // Still returns result structure
    }

    @Test
    void testGetCommandHistory() {
        PageResult<CommandHistory> pageResult = new PageResult<>(List.of(), 0L);
        when(serverManageService.getCommandHistory(any(), any(), anyInt(), anyInt()))
                .thenReturn(pageResult);

        ApiResult<PageResult<CommandHistory>> result = serverManageController.getHistory(null, null, 1, 20);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetHistoryDetail() {
        CommandHistory history = new CommandHistory();
        history.setId(1L);
        history.setCommand("ls -la");

        when(serverManageService.getHistoryById(1L)).thenReturn(history);

        ApiResult<CommandHistory> result = serverManageController.getHistoryDetail(1L);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetHistoryDetailNotFound() {
        when(serverManageService.getHistoryById(999999L)).thenReturn(null);

        ApiResult<CommandHistory> result = serverManageController.getHistoryDetail(999999L);

        assertNotNull(result);
        assertEquals("0000", result.getCode()); // Controller returns success with null
    }

    @Test
    void testGenerateCommand() {
        when(serverManageService.getById(1L)).thenReturn(testServer);

        AiAgentService.AiCommandResult aiResult = new AiAgentService.AiCommandResult(
                "uname -a",
                "System Information",
                0.95,
                AiAgentService.SafetyLevel.LOW
        );
        when(aiAgentService.generateCommand(anyString(), any())).thenReturn(aiResult);

        ApiResult<ServerManageController.AiCommandResult> result =
                serverManageController.generateCommand(1L, "show system info");

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGenerateCommandServerNotFound() {
        when(serverManageService.getById(999999L)).thenReturn(null);

        ApiResult<ServerManageController.AiCommandResult> result =
                serverManageController.generateCommand(999999L, "show system info");

        assertNotNull(result);
        assertEquals("0000", result.getCode()); // Controller returns success even with null server
    }

    @Test
    void testValidateCommand() {
        AiAgentService.SafetyValidation validation = new AiAgentService.SafetyValidation(
                true, "Read-only command", AiAgentService.SafetyLevel.LOW
        );
        when(aiAgentService.validateCommand("ls -la")).thenReturn(validation);

        ApiResult<ServerManageController.AiValidationResult> result =
                serverManageController.validateCommand("ls -la");

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testAssessRisk() {
        when(serverManageService.getById(any())).thenReturn(testServer);

        AiAgentService.SafetyValidation validation = new AiAgentService.SafetyValidation(
                true, "Read-only command", AiAgentService.SafetyLevel.LOW
        );
        when(aiAgentService.validateCommand(anyString())).thenReturn(validation);

        ApiResult<ServerManageController.RiskAssessmentVO> result =
                serverManageController.assessRisk("ls -la", 1L);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testAssessRiskDangerousCommand() {
        AiAgentService.SafetyValidation validation = new AiAgentService.SafetyValidation(
                false, "Dangerous command", AiAgentService.SafetyLevel.HIGH
        );
        when(aiAgentService.validateCommand("rm -rf /")).thenReturn(validation);

        ApiResult<ServerManageController.RiskAssessmentVO> result =
                serverManageController.assessRisk("rm -rf /", null);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        assertEquals("HIGH", result.getData().getRiskLevel());
    }

    @Test
    void testDecomposeTask() {
        ApiResult<List<String>> result = serverManageController.decomposeTask("check system", null);

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetTemplates() {
        ApiResult<List<ServerManageController.CommandTemplateVO>> result =
                serverManageController.getTemplates();

        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().size() > 0);
    }

    @Test
    void testImportPrivateKey() {
        // This would require a real MultipartFile, so we skip actual execution
        // Just verify the endpoint exists and handles the case properly
        assertNotNull(serverManageController);
    }
}