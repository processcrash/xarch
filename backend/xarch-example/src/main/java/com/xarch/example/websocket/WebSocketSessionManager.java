package com.xarch.example.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.xarch.example.entity.ai.Server;
import com.xarch.example.service.ai.SshService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Session Manager for SSH Terminal
 * Manages multiple SSH sessions via WebSocket
 */
@Component
@Slf4j
public class WebSocketSessionManager {

    private final Map<String, SshSession> sessions = new ConcurrentHashMap<>();
    private final SshService sshService;

    public WebSocketSessionManager(SshService sshService) {
        this.sshService = sshService;
    }

    /**
     * Create a new SSH session
     */
    public String createSession(Long serverId) throws JSchException, IOException {
        Server server = sshService.getServer(serverId);
        if (server == null) {
            throw new IllegalArgumentException("Server not found: " + serverId);
        }

        String sessionId = generateSessionId();
        SshSession session = new SshSession(sessionId, serverId, server);
        sessions.put(sessionId, session);

        log.info("Created SSH session: {} for server: {}", sessionId, server.getName());
        return sessionId;
    }

    /**
     * Get session by ID
     */
    public SshSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Remove session
     */
    public void removeSession(String sessionId) {
        SshSession session = sessions.remove(sessionId);
        if (session != null) {
            session.disconnect();
            log.info("Removed SSH session: {}", sessionId);
        }
    }

    /**
     * Execute command in session
     */
    public SshService.CommandResult executeCommand(String sessionId, String command) {
        SshSession session = sessions.get(sessionId);
        if (session == null) {
            return new SshService.CommandResult(-1, "Session not found", 0);
        }
        return session.executeCommand(command);
    }

    /**
     * Resize terminal
     */
    public void resize(String sessionId, int cols, int rows) {
        SshSession session = sessions.get(sessionId);
        if (session != null) {
            session.resize(cols, rows);
        }
    }

    private String generateSessionId() {
        return "ssh-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }

    /**
     * SSH Session wrapper
     */
    @Data
    public static class SshSession {
        private final String sessionId;
        private final Long serverId;
        private final Server server;
        private final Session sshSession;
        private volatile boolean connected = true;

        public SshSession(String sessionId, Long serverId, Server server) throws JSchException, IOException {
            this.sessionId = sessionId;
            this.serverId = serverId;
            this.server = server;
            this.sshSession = SshService.createSession(server);
        }

        public SshService.CommandResult executeCommand(String command) {
            try {
                return SshService.executeCommandOnSession(server, command);
            } catch (Exception e) {
                connected = false;
                return new SshService.CommandResult(-1, "SSH Error: " + e.getMessage(), 0);
            }
        }

        public void resize(int cols, int rows) {
            // Future: implement PTY resize
        }

        public void disconnect() {
            try {
                sshSession.disconnect();
            } catch (Exception e) {
                log.warn("Error disconnecting session: {}", sessionId);
            }
            connected = false;
        }

        @Data
        public static class CommandResult {
            private final int exitCode;
            private final String output;
            private final long duration;

            public CommandResult(int exitCode, String output, long duration) {
                this.exitCode = exitCode;
                this.output = output;
                this.duration = duration;
            }
        }
    }
}