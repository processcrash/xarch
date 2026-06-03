package com.xarch.example.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.jcraft.jsch.Session;
import com.xarch.example.mapper.ai.ServerMapper;
import com.xarch.example.service.ai.SshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler for SSH Terminal
 * Provides real-time SSH command execution via WebSocket
 */
@Component
@Slf4j
public class SshWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, SshSessionHandler> sessionHandlers = new ConcurrentHashMap<>();
    private final SshService sshService;
    private final ServerMapper serverMapper;

    public SshWebSocketHandler(SshService sshService, ServerMapper serverMapper) {
        this.sshService = sshService;
        this.serverMapper = serverMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        session.sendMessage(new TextMessage("{\"type\":\"connected\",\"sessionId\":\"" + session.getId() + "\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received message: {}", payload);

        try {
            JsonMessage jsonMessage = parseMessage(payload);

            switch (jsonMessage.getType()) {
                case "create_session":
                    handleCreateSession(session, jsonMessage);
                    break;
                case "command":
                    handleCommand(session, jsonMessage);
                    break;
                case "resize":
                    handleResize(session, jsonMessage);
                    break;
                case "close":
                    handleClose(session);
                    break;
                default:
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Unknown message type\"}"));
            }
        } catch (Exception e) {
            log.error("Error handling message", e);
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
        SshSessionHandler handler = sessionHandlers.remove(session.getId());
        if (handler != null) {
            handler.close();
        }
    }

    private void handleCreateSession(WebSocketSession session, JsonMessage message) throws IOException {
        Long serverId = message.getLong("serverId");
        if (serverId == null) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"serverId required\"}"));
            return;
        }

        var server = serverMapper.selectById(serverId);
        if (server == null) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Server not found\"}"));
            return;
        }

        try {
            SshService.Session sshSession = SshService.createSession(server);
            SshSessionHandler handler = new SshSessionHandler(session, sshSession);
            sessionHandlers.put(session.getId(), handler);

            session.sendMessage(new TextMessage("{\"type\":\"session_created\",\"serverId\":" + serverId + ",\"serverName\":\"" + server.getName() + "\"}"));
            log.info("SSH session created for server: {}", server.getName());
        } catch (Exception e) {
            log.error("Failed to create SSH session", e);
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Failed to connect: " + e.getMessage() + "\"}"));
        }
    }

    private void handleCommand(WebSocketSession session, JsonMessage message) throws IOException {
        SshSessionHandler handler = sessionHandlers.get(session.getId());
        if (handler == null) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"No active session\"}"));
            return;
        }

        String command = message.getString("command");
        if (command == null || command.isEmpty()) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"command required\"}"));
            return;
        }

        // Execute command and send result
        handler.executeCommand(command, result -> {
            try {
                String json = String.format("{\"type\":\"output\",\"command\":\"%s\",\"exitCode\":%d,\"output\":\"%s\",\"duration\":%d}",
                        escapeJson(command), result.getExitCode(), escapeJson(result.getOutput()), result.getDuration());
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("Failed to send output", e);
            }
        });
    }

    private void handleResize(WebSocketSession session, JsonMessage message) {
        SshSessionHandler handler = sessionHandlers.get(session.getId());
        if (handler != null) {
            int cols = message.getInt("cols");
            int rows = message.getInt("rows");
            handler.resize(cols, rows);
        }
    }

    private void handleClose(WebSocketSession session) throws IOException {
        SshSessionHandler handler = sessionHandlers.remove(session.getId());
        if (handler != null) {
            handler.close();
        }
        session.close();
    }

    private JsonMessage parseMessage(String payload) {
        return JsonMessage.parse(payload);
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }

    /**
     * Simple JSON parser for WebSocket messages
     */
    private static class JsonMessage {
        private final String type;
        private final Map<String, Object> data;

        public JsonMessage(String type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
        }

        public static JsonMessage parse(String json) {
            // Simple JSON parsing - in production use ObjectMapper or similar
            Map<String, Object> data = new ConcurrentHashMap<>();

            // Extract type
            String type = extractString(json, "type");

            // Extract serverId
            Long serverId = extractLong(json, "serverId");
            if (serverId != null) data.put("serverId", serverId);

            // Extract command
            String command = extractString(json, "command");
            if (command != null) data.put("command", command);

            // Extract cols/rows
            Integer cols = extractInt(json, "cols");
            if (cols != null) data.put("cols", cols);
            Integer rows = extractInt(json, "rows");
            if (rows != null) data.put("rows", rows);

            return new JsonMessage(type, data);
        }

        private static String extractString(String json, String key) {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        }

        private static Long extractLong(String json, String key) {
            String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
            return null;
        }

        private static Integer extractInt(String json, String key) {
            String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            return null;
        }

        public String getType() { return type; }
        public Object get(String key) { return data.get(key); }
        public String getString(String key) { return (String) data.get(key); }
        public Long getLong(String key) { return (Long) data.get(key); }
        public Integer getInt(String key) { return (Integer) data.get(key); }
    }

    /**
     * SSH Session Handler
     */
    private static class SshSessionHandler {
        private final WebSocketSession session;
        private final Session sshSession;
        private boolean executing = false;

        public SshSessionHandler(WebSocketSession session, Session sshSession) {
            this.session = session;
            this.sshSession = sshSession;
        }

        public synchronized void executeCommand(String command, CommandCallback callback) {
            if (executing) {
                try {
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Command already executing\"}"));
                } catch (IOException e) {
                    log.error("Failed to send message", e);
                }
                return;
            }

            executing = true;
            new Thread(() -> {
                try {
                    SshService.CommandResult result = sshSession.executeCommand(command);
                    callback.onResult(result);
                } catch (Exception e) {
                    try {
                        session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
                    } catch (IOException ex) {
                        log.error("Failed to send error", ex);
                    }
                } finally {
                    executing = false;
                }
            }).start();
        }

        public void resize(int cols, int rows) {
            // Future: implement PTY resize
        }

        public void close() {
            try {
                sshSession.disconnect();
            } catch (Exception e) {
                log.warn("Error closing SSH session", e);
            }
        }

        @FunctionalInterface
        interface CommandCallback {
            void onResult(SshService.CommandResult result);
        }
    }
}