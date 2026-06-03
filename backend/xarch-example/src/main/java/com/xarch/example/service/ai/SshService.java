package com.xarch.example.service.ai;

import com.jcraft.jsch.*;
import com.xarch.example.entity.ai.Server;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH Service for remote command execution
 * Thread-safe connection pool for multiple servers
 */
@Service
public class SshService {

    // Connection pool: serverId -> JSch session
    private final Map<Long, Session> sessionPool = new ConcurrentHashMap<>();

    /**
     * Connect to server and return session
     */
    public Session connect(Server server) throws JSchException {
        // Check if already connected
        Session existingSession = sessionPool.get(server.getId());
        if (existingSession != null && existingSession.isConnected()) {
            return existingSession;
        }

        JSch jsch = new JSch();

        // Add private key if using key authentication
        if ("key".equals(server.getAuthType()) && server.getPrivateKey() != null) {
            jsch.addIdentity("server_key", server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                    null, server.getPassphrase() != null ? server.getPassphrase().getBytes(StandardCharsets.UTF_8) : null);
        }

        Session session = jsch.getSession(server.getUsername(), server.getHost(), server.getPort() != null ? server.getPort() : 22);
        session.setConfig("StrictHostKeyChecking", "no");

        // Set password if using password authentication
        if ("password".equals(server.getAuthType())) {
            session.setPassword(server.getPassword());
        }

        session.connect(30000); // 30 second timeout

        sessionPool.put(server.getId(), session);
        return session;
    }

    /**
     * Execute command on server
     */
    public CommandResult executeCommand(Server server, String command) throws JSchException {
        long startTime = System.currentTimeMillis();

        Session session = connect(server);
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        channel.setOutputStream(outputStream);

        channel.connect(30000);

        // Wait for command completion
        while (!channel.isClosed()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        int exitCode = channel.getExitStatus();
        String output = outputStream.toString(StandardCharsets.UTF_8);

        long duration = System.currentTimeMillis() - startTime;

        channel.disconnect();

        return new CommandResult(exitCode, output, duration);
    }

    /**
     * Execute command with PTY (pseudo-terminal) for interactive commands
     */
    public CommandResult executePtyCommand(Server server, String command) throws JSchException {
        long startTime = System.currentTimeMillis();

        Session session = connect(server);
        ChannelShell channel = (ChannelShell) session.openChannel("shell");

        ByteArrayInputStream inputStream = new ByteArrayInputStream((command + "\nexit\n").getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        channel.setInputStream(inputStream);
        channel.setOutputStream(outputStream);
        channel.setPty(true);

        channel.connect(30000);

        // Wait for command completion
        while (!channel.isClosed()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        int exitCode = channel.getExitStatus();
        String output = outputStream.toString(StandardCharsets.UTF_8);

        long duration = System.currentTimeMillis() - startTime;

        channel.disconnect();

        return new CommandResult(exitCode, output, duration);
    }

    /**
     * Test connection to server
     */
    public boolean testConnection(Server server) {
        try {
            Session session = connect(server);
            return session.isConnected();
        } catch (JSchException e) {
            return false;
        }
    }

    /**
     * Disconnect from server
     */
    public void disconnect(Long serverId) {
        Session session = sessionPool.get(serverId);
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        sessionPool.remove(serverId);
    }

    /**
     * Disconnect all sessions
     */
    public void disconnectAll() {
        for (Map.Entry<Long, Session> entry : sessionPool.entrySet()) {
            if (entry.getValue().isConnected()) {
                entry.getValue().disconnect();
            }
        }
        sessionPool.clear();
    }

    /**
     * Check if server is connected
     */
    public boolean isConnected(Long serverId) {
        Session session = sessionPool.get(serverId);
        return session != null && session.isConnected();
    }

    /**
     * Get server by ID (for session management)
     */
    public Server getServer(Long serverId) {
        // This would be replaced with actual server lookup from DB
        // For now, return null and let caller handle
        return null;
    }

    /**
     * Execute command on an existing SSH session (for WebSocket)
     */
    public static CommandResult executeCommandOnSession(Server server, String command) throws JSchException {
        return new SshService().executeCommand(server, command);
    }

    /**
     * Create SSH session for WebSocket terminal
     */
    public static Session createSession(Server server) throws JSchException {
        JSch jsch = new JSch();

        // Add private key if using key authentication
        if ("key".equals(server.getAuthType()) && server.getPrivateKey() != null) {
            jsch.addIdentity("server_key", server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                    null, server.getPassphrase() != null ? server.getPassphrase().getBytes(StandardCharsets.UTF_8) : null);
        }

        Session session = jsch.getSession(server.getUsername(), server.getHost(), server.getPort() != null ? server.getPort() : 22);
        session.setConfig("StrictHostKeyChecking", "no");

        // Set password if using password authentication
        if ("password".equals(server.getAuthType())) {
            session.setPassword(server.getPassword());
        }

        session.connect(30000); // 30 second timeout
        return session;
    }

    /**
     * Command execution result
     */
    public static class CommandResult {
        private final int exitCode;
        private final String output;
        private final long duration;

        public CommandResult(int exitCode, String output, long duration) {
            this.exitCode = exitCode;
            this.output = output;
            this.duration = duration;
        }

        public int getExitCode() { return exitCode; }
        public String getOutput() { return output; }
        public long getDuration() { return duration; }
        public boolean isSuccess() { return exitCode == 0; }
    }
}