package com.xarch.example.service.ai;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
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
    private CommandHistoryMapper commandHistoryMapper;

    @Autowired
    private SshService sshService;

    @Autowired
    private AiAgentService aiAgentService;

    /**
     * Page query servers
     */
    public PageResult<Server> page(String keyword, String serverGroup, Integer status, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("ai_server").where("del_flag = 0");

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and("(name LIKE ? OR host LIKE ? OR description LIKE ?)",
                    "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%");
        }
        if (serverGroup != null && !serverGroup.isEmpty()) {
            wrapper.and("server_group = ?", serverGroup);
        }
        if (status != null) {
            wrapper.and("status = ?", status);
        }
        wrapper.orderBy("create_time", false);

        Page<Server> page = serverMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
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
        history.setUserIp("127.0.0.1");
        history.setDelFlag(0);

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

        commandHistoryMapper.insert(history);

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
        QueryWrapper wrapper = QueryWrapper.create().from("ai_command_history").where("del_flag = 0");

        if (serverId != null) {
            wrapper.and("server_id = ?", serverId);
        }
        if (sessionId != null && !sessionId.isEmpty()) {
            wrapper.and("session_id = ?", sessionId);
        }
        wrapper.orderBy("create_time", false);

        Page<CommandHistory> page = commandHistoryMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    /**
     * Get command history by ID
     */
    public CommandHistory getHistoryById(Long id) {
        return commandHistoryMapper.selectById(id);
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