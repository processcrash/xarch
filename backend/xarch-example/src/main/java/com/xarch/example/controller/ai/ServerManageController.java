package com.xarch.example.controller.ai;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.entity.ai.CommandHistory;
import com.xarch.example.entity.ai.Server;
import com.xarch.example.service.ai.AiAgentService;
import com.xarch.example.service.ai.ServerManageService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Server Management Controller
 * Linux server remote management with AI Agent
 */
@RestController
@RequestMapping("/ai/server")
@Tag(name = "AI Server Management", description = "Linux server remote management with AI Agent")
public class ServerManageController {

    @Autowired
    private ServerManageService serverManageService;

    @Autowired
    private AiAgentService aiAgentService;

    // ==================== Server CRUD ====================

    @GetMapping("/page")
    @Operation(summary = "Page query servers")
    public ApiResult<PageResult<Server>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String serverGroup,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.success(serverManageService.page(keyword, serverGroup, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get server details")
    public ApiResult<Server> detail(@PathVariable Long id) {
        Server server = serverManageService.getById(id);
        if (server == null) {
            return ApiResult.fail("Server not found");
        }
        // Mask password for security
        server.setPassword(null);
        server.setPrivateKey(null);
        return ApiResult.success(server);
    }

    @PostMapping
    @Operation(summary = "Create server")
    public ApiResult<Void> create(@RequestBody Server server) {
        serverManageService.create(server);
        return ApiResult.success(null);
    }

    @PutMapping
    @Operation(summary = "Update server")
    public ApiResult<Void> update(@RequestBody Server server) {
        serverManageService.update(server);
        return ApiResult.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete server")
    public ApiResult<Void> delete(@PathVariable Long id) {
        serverManageService.delete(id);
        return ApiResult.success(null);
    }

    @PostMapping("/{id}/connect")
    @Operation(summary = "Connect to server")
    public ApiResult<Boolean> connect(@PathVariable Long id) {
        return ApiResult.success(serverManageService.connect(id));
    }

    @PostMapping("/{id}/disconnect")
    @Operation(summary = "Disconnect from server")
    public ApiResult<Void> disconnect(@PathVariable Long id) {
        serverManageService.disconnect(id);
        return ApiResult.success(null);
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test server connection")
    public ApiResult<Boolean> testConnection(@PathVariable Long id) {
        Server server = serverManageService.getById(id);
        if (server == null) {
            return ApiResult.fail("Server not found");
        }
        return ApiResult.success(serverManageService.testConnection(server));
    }

    @PostMapping("/import-key")
    @Operation(summary = "Import SSH private key")
    public ApiResult<String> importPrivateKey(@RequestParam("file") MultipartFile file) {
        try {
            String privateKey = serverManageService.importPrivateKey(file);
            return ApiResult.success(privateKey);
        } catch (Exception e) {
            return ApiResult.fail("Failed to import key: " + e.getMessage());
        }
    }

    // ==================== Command Execution ====================

    @PostMapping("/command")
    @Operation(summary = "Execute command on server")
    public ApiResult<CommandHistory> executeCommand(@RequestBody CommandRequest request) {
        try {
            CommandHistory history = serverManageService.executeCommand(request);
            return ApiResult.success(history);
        } catch (Exception e) {
            return ApiResult.fail("Command execution failed: " + e.getMessage());
        }
    }

    @PostMapping("/command/ai")
    @Operation(summary = "Execute AI-generated command")
    public ApiResult<CommandHistory> executeAiCommand(@RequestBody AiCommandRequest request) {
        try {
            CommandHistory history = serverManageService.executeAiCommand(
                    request.getServerId(),
                    request.getNaturalLanguage(),
                    request.getSessionId()
            );
            return ApiResult.success(history);
        } catch (Exception e) {
            return ApiResult.fail("AI command execution failed: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get command history")
    public ApiResult<PageResult<CommandHistory>> getHistory(
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(serverManageService.getCommandHistory(serverId, sessionId, pageNum, pageSize));
    }

    @GetMapping("/history/{id}")
    @Operation(summary = "Get command history detail")
    public ApiResult<CommandHistory> getHistoryDetail(@PathVariable Long id) {
        // commandHistoryMapper.selectById(id)
        return ApiResult.success(null);
    }

    // ==================== AI Agent ====================

    @PostMapping("/ai/generate")
    @Operation(summary = "Generate command from natural language")
    public ApiResult<AiCommandResult> generateCommand(
            @RequestParam Long serverId,
            @RequestParam String naturalLanguage) {
        Server server = serverManageService.getById(serverId);
        if (server == null) {
            return ApiResult.fail("Server not found");
        }

        AiAgentService.AiCommandResult result = aiAgentService.generateCommand(naturalLanguage, server);
        return ApiResult.success(new AiCommandResult(
                result.getCommand(),
                result.getCategory(),
                result.getConfidence(),
                result.getSafetyLevel().name()
        ));
    }

    @PostMapping("/ai/validate")
    @Operation(summary = "Validate command safety")
    public ApiResult<AiValidationResult> validateCommand(@RequestParam String command) {
        AiAgentService.SafetyValidation validation = aiAgentService.validateCommand(command);
        return ApiResult.success(new AiValidationResult(
                validation.isSafe(),
                validation.getMessage(),
                validation.getLevel().name()
        ));
    }

    @PostMapping("/ai/risk")
    @Operation(summary = "Assess command risk level")
    public ApiResult<RiskAssessmentVO> assessRisk(@RequestParam String command, @RequestParam(required = false) Long serverId) {
        Server server = serverId != null ? serverManageService.getById(serverId) : null;
        AiAgentService.SafetyValidation validation = aiAgentService.validateCommand(command);

        RiskLevel riskLevel;
        String action;
        if (validation.getLevel() == AiAgentService.SafetyLevel.HIGH) {
            riskLevel = RiskLevel.HIGH;
            action = "blocked";
        } else if (validation.getLevel() == AiAgentService.SafetyLevel.MEDIUM) {
            riskLevel = RiskLevel.MEDIUM;
            action = "warning";
        } else if (validation.isSafe()) {
            riskLevel = RiskLevel.LOW;
            action = "allowed";
        } else {
            riskLevel = RiskLevel.MEDIUM;
            action = "warning";
        }

        return ApiResult.success(new RiskAssessmentVO(
                riskLevel.name(),
                validation.getMessage(),
                action
        ));
    }

    @PostMapping("/ai/decompose")
    @Operation(summary = "Decompose multi-step task into commands")
    public ApiResult<List<String>> decomposeTask(@RequestParam String naturalLanguage, @RequestParam(required = false) Long serverId) {
        Server server = serverId != null ? serverManageService.getById(serverId) : null;
        // Use AiAgentService for task decomposition
        return ApiResult.success(List.of("step1 command", "step2 command"));
    }

    @GetMapping("/ai/templates")
    @Operation(summary = "Get AI command templates")
    public ApiResult<List<CommandTemplateVO>> getTemplates() {
        // Return common command templates
        List<CommandTemplateVO> templates = List.of(
                new CommandTemplateVO("system_info", "Show system information", "uname -a && cat /etc/os-release", "System Information"),
                new CommandTemplateVO("cpu_info", "Show CPU information", "lscpu && top -bn1 | head -20", "System Information"),
                new CommandTemplateVO("memory_info", "Show memory usage", "free -h && cat /proc/meminfo | head -10", "System Information"),
                new CommandTemplateVO("disk_usage", "Show disk usage", "df -h && lsblk", "Storage"),
                new CommandTemplateVO("process_list", "Show running processes", "ps aux | head -30", "Process Management"),
                new CommandTemplateVO("network", "Show network connections", "netstat -tulpn | head -30", "Network"),
                new CommandTemplateVO("service_status", "Show service status", "systemctl list-units --type=service --state=running | head -30", "Service Management"),
                new CommandTemplateVO("user_list", "Show all users", "cat /etc/passwd | grep /bin/bash | cut -d: -f1", "User Management"),
                new CommandTemplateVO("docker_status", "Show Docker status", "docker ps -a && docker images", "Container"),
                new CommandTemplateVO("nginx_status", "Show Nginx status", "systemctl status nginx && ps aux | grep nginx", "Web Server")
        );
        return ApiResult.success(templates);
    }

    @Data
    public static class CommandRequest {
        private Long serverId;
        private String command;
        private String sessionId;
    }

    @Data
    public static class AiCommandRequest {
        private Long serverId;
        private String naturalLanguage;
        private String sessionId;
    }

    @Data
    public static class AiCommandResult {
        private String command;
        private String category;
        private double confidence;
        private String safetyLevel;

        public AiCommandResult(String command, String category, double confidence, String safetyLevel) {
            this.command = command;
            this.category = category;
            this.confidence = confidence;
            this.safetyLevel = safetyLevel;
        }
    }

    @Data
    public static class AiValidationResult {
        private boolean isSafe;
        private String message;
        private String safetyLevel;

        public AiValidationResult(boolean isSafe, String message, String safetyLevel) {
            this.isSafe = isSafe;
            this.message = message;
            this.safetyLevel = safetyLevel;
        }
    }

    @Data
    public static class CommandTemplateVO {
        private String id;
        private String name;
        private String command;
        private String category;

        public CommandTemplateVO(String id, String name, String command, String category) {
            this.id = id;
            this.name = name;
            this.command = command;
            this.category = category;
        }
    }

    @Data
    public static class RiskAssessmentVO {
        private String riskLevel;
        private String message;
        private String action;

        public RiskAssessmentVO(String riskLevel, String message, String action) {
            this.riskLevel = riskLevel;
            this.message = message;
            this.action = action;
        }
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
}