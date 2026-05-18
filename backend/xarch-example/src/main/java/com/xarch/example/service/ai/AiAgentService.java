package com.xarch.example.service.ai;

import com.xarch.example.entity.ai.Server;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI Agent for Linux Server Management
 * Generates shell commands from natural language descriptions
 *
 * Features:
 * - Natural language to shell command conversion
 * - Server context awareness
 * - Safety validation
 * - Multi-step task decomposition
 */
@Service
@Slf4j
public class AiAgentService {

    // Command templates for common operations
    private static final Map<String, CommandTemplate> COMMAND_TEMPLATES = new HashMap<>();

    // Common Linux commands and their natural language patterns
    private static final List<PatternMapping> PATTERN_MAPPINGS = new ArrayList<>();

    static {
        initializePatterns();
    }

    /**
     * Generate command from natural language
     */
    public AiCommandResult generateCommand(String naturalLanguage, Server server) {
        String lowerInput = naturalLanguage.toLowerCase().trim();

        // Match patterns to find appropriate command
        for (PatternMapping mapping : PATTERN_MAPPINGS) {
            if (mapping.matches(lowerInput)) {
                String command = mapping.generateCommand(lowerInput, server);
                return new AiCommandResult(command, mapping.category, mapping.confidence, mapping.safetyLevel);
            }
        }

        // Fallback: return the input as-is if it looks like a command
        if (isLikelyShellCommand(naturalLanguage)) {
            return new AiCommandResult(naturalLanguage, "manual", 0.5, SafetyLevel.WARNING);
        }

        // Cannot generate
        return new AiCommandResult(null, "unknown", 0.0, SafetyLevel.HIGH);
    }

    /**
     * Validate if a command is safe to execute
     */
    public SafetyValidation validateCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return new SafetyValidation(false, "Empty command", SafetyLevel.HIGH);
        }

        String lowerCommand = command.toLowerCase();

        // Dangerous commands that should never be executed
        if (containsDangerousPattern(lowerCommand)) {
            return new SafetyValidation(false, "Dangerous command detected", SafetyLevel.HIGH);
        }

        // Commands requiring confirmation
        if (requiresConfirmation(lowerCommand)) {
            return new SafetyValidation(true, "This command requires confirmation", SafetyLevel.MEDIUM);
        }

        // Read-only or safe commands
        if (isReadOnlyCommand(command)) {
            return new SafetyValidation(true, "Read-only command", SafetyLevel.LOW);
        }

        return new SafetyValidation(true, "Command appears safe", SafetyLevel.LOW);
    }

    private boolean containsDangerousPattern(String command) {
        String[] dangerousPatterns = {
            "rm -rf /",
            "rm -rf /*",
            "dd if=",
            ":(){:|:&};:",  // Fork bomb
            "> /dev/sda",
            "mkfs.",
            "fdisk /dev/",
            "dd of=/dev/",
            "wget | sh",
            "curl | sh",
            "chmod -r 777 /",
            "chmod 000",
            "shutdown",
            "reboot -f",
            "init 0",
            "init 6",
            "--no-preserve-root",
            "eval `",
            "exec `"
        };

        for (String pattern : dangerousPatterns) {
            if (command.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiresConfirmation(String command) {
        String[] confirmationCommands = {
            "rm -r", "rm -f", "rm --",
            "shutdown", "reboot",
            "service restart", "systemctl restart",
            "kill -9", "killall",
            "drop table", "delete from",
            "truncate", "drop database"
        };

        for (String cmd : confirmationCommands) {
            if (command.contains(cmd)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLikelyShellCommand(String input) {
        String[] safeCommands = {"ls", "cd", "cat", "grep", "ps", "top", "df", "du", "pwd", "who", "last", "head", "tail", "wc"};
        for (String cmd : safeCommands) {
            if (input.trim().startsWith(cmd)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReadOnlyCommand(String command) {
        String[] readOnlyCommands = {"ls", "cat", "head", "tail", "grep", "wc", "ps", "df", "du", "who", "last", "uptime", "uname", "hostname"};
        for (String cmd : readOnlyCommands) {
            if (command.trim().split("\\s+")[0].equals(cmd)) {
                return true;
            }
        }
        return false;
    }

    private static void initializePatterns() {
        // System info commands
        PATTERN_MAPPINGS.add(new PatternMapping(
                "system info|show system|server info|os version|linux version",
                "uname -a && cat /etc/os-release",
                "System Information",
                0.95, SafetyLevel.LOW
        ));

        // CPU info
        PATTERN_MAPPINGS.add(new PatternMapping(
                "cpu info|show cpu|processor|cpu usage",
                "lscpu && top -bn1 | head -20",
                "CPU Information",
                0.95, SafetyLevel.LOW
        ));

        // Memory info
        PATTERN_MAPPINGS.add(new PatternMapping(
                "memory info|ram usage|show memory",
                "free -h && cat /proc/meminfo | head -10",
                "Memory Information",
                0.95, SafetyLevel.LOW
        ));

        // Disk usage
        PATTERN_MAPPINGS.add(new PatternMapping(
                "disk usage|disk space|storage|show disk",
                "df -h && lsblk",
                "Disk Usage",
                0.95, SafetyLevel.LOW
        ));

        // Process list
        PATTERN_MAPPINGS.add(new PatternMapping(
                "process list|show process|running processes",
                "ps aux | head -30",
                "Process List",
                0.95, SafetyLevel.LOW
        ));

        // Network connections
        PATTERN_MAPPINGS.add(new PatternMapping(
                "network|connections|show netstat|ports",
                "netstat -tulpn | head -30",
                "Network Information",
                0.95, SafetyLevel.LOW
        ));

        // Package list (Debian/Ubuntu)
        PATTERN_MAPPINGS.add(new PatternMapping(
                "installed packages|package list|dpkg",
                "dpkg -l | head -30",
                "Package List",
                0.95, SafetyLevel.LOW
        ));

        // Package list (RHEL/CentOS)
        PATTERN_MAPPINGS.add(new PatternMapping(
                "installed packages|package list|rpm",
                "rpm -qa | head -30",
                "Package List",
                0.95, SafetyLevel.LOW
        ));

        // Service status
        PATTERN_MAPPINGS.add(new PatternMapping(
                "service status|systemctl|show service",
                "systemctl list-units --type=service --state=running | head -30",
                "Service Status",
                0.90, SafetyLevel.LOW
        ));

        // User list
        PATTERN_MAPPINGS.add(new PatternMapping(
                "user list|show users|all users",
                "cat /etc/passwd | grep /bin/bash | cut -d: -f1",
                "User List",
                0.90, SafetyLevel.LOW
        ));

        // Disk I/O
        PATTERN_MAPPINGS.add(new PatternMapping(
                "disk io|io stats|iostat",
                "iostat -x 1 5",
                "Disk I/O",
                0.90, SafetyLevel.LOW
        ));

        // Log files
        PATTERN_MAPPINGS.add(new PatternMapping(
                "system log|kernel log|syslog|show log",
                "tail -100 /var/log/syslog",
                "System Logs",
                0.90, SafetyLevel.LOW
        ));

        // Nginx status
        PATTERN_MAPPINGS.add(new PatternMapping(
                "nginx status|nginx process",
                "systemctl status nginx && ps aux | grep nginx",
                "Nginx Status",
                0.90, SafetyLevel.LOW
        ));

        // Docker status
        PATTERN_MAPPINGS.add(new PatternMapping(
                "docker status|docker containers|docker ps",
                "docker ps -a && docker images",
                "Docker Status",
                0.90, SafetyLevel.LOW
        ));

        // systemctl start service
        PATTERN_MAPPINGS.add(new PatternMapping(
                "start (\\w+) service|start (\\w+)",
                "systemctl start $1",
                "Service Control",
                0.85, SafetyLevel.MEDIUM
        ));

        // systemctl stop service
        PATTERN_MAPPINGS.add(new PatternMapping(
                "stop (\\w+) service|stop (\\w+)",
                "systemctl stop $1",
                "Service Control",
                0.85, SafetyLevel.MEDIUM
        ));

        // systemctl restart service
        PATTERN_MAPPINGS.add(new PatternMapping(
                "restart (\\w+) service|restart (\\w+)",
                "systemctl restart $1",
                "Service Control",
                0.85, SafetyLevel.MEDIUM
        ));

        // cd directory
        PATTERN_MAPPINGS.add(new PatternMapping(
                "cd |change directory to |go to |navigate to ",
                "$1",
                "Directory Navigation",
                0.90, SafetyLevel.LOW
        ));

        // ls directory
        PATTERN_MAPPINGS.add(new PatternMapping(
                "ls |list files|list directory|show files",
                "ls -la $1",
                "Directory Listing",
                0.95, SafetyLevel.LOW
        ));

        // Find file
        PATTERN_MAPPINGS.add(new PatternMapping(
                "find file|search for file|find (\\w+)",
                "find / -name \"$1\" 2>/dev/null | head -20",
                "File Search",
                0.90, SafetyLevel.LOW
        ));

        // Grep
        PATTERN_MAPPINGS.add(new PatternMapping(
                "grep |search in |find in file",
                "grep -r \"$1\" /var/log 2>/dev/null | tail -20",
                "Search Content",
                0.90, SafetyLevel.LOW
        ));
    }

    /**
     * Command template for parameterized commands
     */
    @Data
    private static class CommandTemplate {
        private String pattern;
        private String template;
        private String category;
        private double confidence;
        private SafetyLevel safetyLevel;
    }

    /**
     * Pattern mapping for natural language to command
     */
    private static class PatternMapping {
        String pattern;
        String commandTemplate;
        String category;
        double confidence;
        SafetyLevel safetyLevel;
        Pattern compiledPattern;

        PatternMapping(String pattern, String command, String category, double confidence, SafetyLevel safetyLevel) {
            this.pattern = pattern;
            this.commandTemplate = command;
            this.category = category;
            this.confidence = confidence;
            this.safetyLevel = safetyLevel;
            this.compiledPattern = Pattern.compile(pattern);
        }

        boolean matches(String input) {
            return compiledPattern.matcher(input).find();
        }

        String generateCommand(String input, Server server) {
            Matcher matcher = compiledPattern.matcher(input);
            String result = commandTemplate;

            // Extract captured groups and replace placeholders
            if (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String group = matcher.group(i);
                    if (group != null) {
                        result = result.replace("$" + i, group);
                    }
                }
            }

            // Add server-specific context if available
            if (server != null && server.getOsType() != null) {
                // Adjust command based on OS type
                if (server.getOsType().toLowerCase().contains("centos") ||
                    server.getOsType().toLowerCase().contains("rhel")) {
                    result = result.replace("dpkg -l", "rpm -qa");
                }
            }

            return result;
        }
    }

    /**
     * AI Command generation result
     */
    @Data
    public static class AiCommandResult {
        private String command;
        private String category;
        private double confidence;
        private SafetyLevel safetyLevel;

        public AiCommandResult(String command, String category, double confidence, SafetyLevel safetyLevel) {
            this.command = command;
            this.category = category;
            this.confidence = confidence;
            this.safetyLevel = safetyLevel;
        }
    }

    /**
     * Safety validation result
     */
    @Data
    public static class SafetyValidation {
        private boolean isSafe;
        private String message;
        private SafetyLevel level;

        public SafetyValidation(boolean isSafe, String message, SafetyLevel level) {
            this.isSafe = isSafe;
            this.message = message;
            this.level = level;
        }
    }

    /**
     * Safety level enum
     */
    public enum SafetyLevel {
        LOW,      // Read-only, safe
        MEDIUM,   // May modify state, needs confirmation
        WARNING,  // Potentially dangerous
        HIGH      // Dangerous, should not execute
    }
}