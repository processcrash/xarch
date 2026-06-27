package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.CommandHistory;
import com.xarch.example.ai.entity.Server;
import com.xarch.starter.core.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** AI server management service contract. */
public interface ServerManageService {
    PageResult<Server> page(String keyword, String serverGroup, Integer status, int pageNum, int pageSize);
    Server getById(Long id);
    void create(Server server);
    void update(Server server);
    void delete(Long id);
    boolean connect(Long id);
    void disconnect(Long id);
    boolean testConnection(Server server);
    String importPrivateKey(MultipartFile file) throws IOException;
    CommandHistory executeCommand(CommandRequest request);
    CommandHistory executeAiCommand(Long serverId, String naturalLanguage, String sessionId);
    PageResult<CommandHistory> getCommandHistory(Long serverId, String sessionId, int pageNum, int pageSize);
    CommandHistory getHistoryById(Long id);

    /** Command execution request. */
    final class CommandRequest {
        private Long serverId;
        private String command;
        private String sessionId;

        public Long getServerId() { return serverId; }
        public void setServerId(Long serverId) { this.serverId = serverId; }
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}