package com.xarch.example.service.ai;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.ai.CommandHistory;
import com.xarch.example.entity.ai.Server;
import com.xarch.example.mapper.ai.ServerMapper;
import com.xarch.starter.core.result.PageResult;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Server Management Service
 * Handles CRUD operations and command execution
 */
@Service
public class ServerManageService {

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private SshService sshService;

    @Autowired
    private AiAgentService aiAgentService;

    /**
     * Page query servers
     */
    public PageResult<Server> page(String keyword, String serverGroup, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Server> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Server::getDelFlag, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Server::getName, keyword)
                    .or().like(Server::getHost, keyword)
                    .or().like(Server::getDescription, keyword));
        }
        if (serverGroup != null && !serverGroup.isEmpty()) {
            wrapper.eq(Server::getServerGroup, serverGroup);
        }
        if (status != null) {
            wrapper.eq(Server::getStatus, status);
        }

        wrapper.orderByDesc(Server::getCreateTime);

        Page<Server> page = new Page<>(pageNum, pageSize);
        Page<Server> result = serverMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    /**
     * Get server by ID
     */
    public Server getById(Long id) {
        return serverMapper.selectById(id);
    }

    /**
     * Create server
     */
    public void create(Server server) {
        // Test connection before saving
        if (testConnection(server)) {
            server.setStatus(1);
            server.setLastConnectedTime(LocalDateTime.now());
        } else {
            server.setStatus(2);
        }
        server.setDelFlag(0);
        serverMapper.insert(server);
    }

    /**
     * Update server
     */
    public void update(Server server) {
        serverMapper.updateById(server);
    }

    /**
     * Delete server (soft delete)
     */
    public void delete(Long id) {
        Server server = serverMapper.selectById(id);
        if (server != null) {
            // Disconnect first
            sshService.disconnect(id);
            server.setDelFlag(1);
            serverMapper.updateById(server);
        }
    }

    /**
     * Test connection
     */
    public boolean testConnection(Server server) {
        return sshService.testConnection(server);
    }

    /**
     * Connect to server
     */
    public boolean connect(Long serverId) {
        Server server = serverMapper.selectById(serverId);
        if (server == null) return false;

        boolean connected = sshService.testConnection(server);
        if (connected) {
            server.setStatus(1);
            server.setLastConnectedTime(LocalDateTime.now());
            server.setLastError(null);
        } else {
            server.setStatus(2);
        }
        serverMapper.updateById(server);
        return connected;
    }

    /**
     * Disconnect from server
     */
    public void disconnect(Long serverId) {
        sshService.disconnect(serverId);
        Server server = serverMapper.selectById(serverId);
        if (server != null) {
            server.setStatus(0);
            serverMapper.updateById(server);
        }
    }

    /**
     * Execute command on server
     */
    @Data
    public static class CommandRequest {
        private Long serverId;
        private String command;
        private String sessionId;
    }

    /**
     * Execute command and record history
     */
    public CommandHistory executeCommand(CommandRequest request) {
        Server server = serverMapper.selectById(request.getServerId());
        if (server == null) {
            throw new RuntimeException("Server not found");
        }

        CommandHistory history = new CommandHistory();
        history.setServerId(server.getId());
        history.setServerName(server.getName());
        history.setUserId(StpUtil.getLoginIdAsLong());
        history.setUserName(StpUtil.getLoginIdAsString());
        history.setCommand(request.getCommand());
        history.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
        history.setUserIp("127.0.0.1"); // TODO: Get from request context

        try {
            SshService.CommandResult result = sshService.executeCommand(server, request.getCommand());
            history.setExitCode(result.getExitCode());
            history.setOutput(result.getOutput());
            history.setDuration(result.getDuration());
            history.setStatus(result.isSuccess() ? 1 : 2);
        } catch (Exception e) {
            history.setExitCode(-1);
            history.setOutput("Error: " + e.getMessage());
            history.setStatus(2);
            history.setDuration(0L);
        }

        // Save history (would use CommandHistoryMapper)
        // commandHistoryMapper.insert(history);

        return history;
    }

    /**
     * AI command generation and execution
     */
    public CommandHistory executeAiCommand(Long serverId, String naturalLanguage, String sessionId) {
        Server server = serverMapper.selectById(serverId);
        if (server == null) {
            throw new RuntimeException("Server not found");
        }

        // Generate command using AI
        AiAgentService.AiCommandResult aiResult = aiAgentService.generateCommand(naturalLanguage, server);

        if (aiResult.getCommand() == null) {
            throw new RuntimeException("Cannot generate command from: " + naturalLanguage);
        }

        CommandHistory history = new CommandHistory();
        history.setServerId(server.getId());
        history.setServerName(server.getName());
        history.setUserId(StpUtil.getLoginIdAsLong());
        history.setUserName(StpUtil.getLoginIdAsString());
        history.setAiPrompt(naturalLanguage);
        history.setAiGeneratedCommand(aiResult.getCommand());
        history.setCommand(aiResult.getCommand());
        history.setSessionId(sessionId != null ? sessionId : UUID.randomUUID().toString());
        history.setUserIp("127.0.0.1");

        try {
            SshService.CommandResult result = sshService.executeCommand(server, aiResult.getCommand());
            history.setExitCode(result.getExitCode());
            history.setOutput(result.getOutput());
            history.setDuration(result.getDuration());
            history.setStatus(result.isSuccess() ? 1 : 2);
        } catch (Exception e) {
            history.setExitCode(-1);
            history.setOutput("Error: " + e.getMessage());
            history.setStatus(2);
        }

        return history;
    }

    /**
     * Get command history for server
     */
    public PageResult<CommandHistory> getCommandHistory(Long serverId, String sessionId, int pageNum, int pageSize) {
        LambdaQueryWrapper<CommandHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommandHistory::getDelFlag, 0);

        if (serverId != null) {
            wrapper.eq(CommandHistory::getServerId, serverId);
        }
        if (sessionId != null && !sessionId.isEmpty()) {
            wrapper.eq(CommandHistory::getSessionId, sessionId);
        }

        wrapper.orderByDesc(CommandHistory::getCreateTime);

        Page<CommandHistory> page = new Page<>(pageNum, pageSize);
        // Page<CommandHistory> result = commandHistoryMapper.selectPage(page, wrapper);

        // return PageResult.of(result.getRecords(), result.getTotal());
        return null; // Placeholder
    }

    /**
     * Import private key from file
     */
    public String importPrivateKey(MultipartFile file) throws IOException {
        Path tempDir = Paths.get("/tmp/xarch-keys");
        Files.createDirectories(tempDir);

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = tempDir.resolve(filename);

        Files.copy(file.getInputStream(), filePath);

        return Files.readString(filePath);
    }
}