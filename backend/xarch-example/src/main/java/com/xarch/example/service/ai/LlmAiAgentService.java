package com.xarch.example.service.ai;

import com.xarch.example.entity.ai.Server;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced AI Agent using LLM for natural language to shell command conversion
 * Falls back to pattern matching if LLM is not available
 */
@Service
@Slf4j
public class LlmAiAgentService {

    // Pattern-based fallback mappings
    private static final List<PatternMapping> PATTERN_MAPPINGS = new ArrayList<>();

    static {
        initializePatterns();
    }

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-4}")
    private String model;

    /**
     * Generate command from natural language using LLM with fallback to pattern matching
     */
    public AiCommandResult generateCommand(String naturalLanguage, Server server) {
        String lowerInput = naturalLanguage.toLowerCase().trim();

        // Try LLM first if API key is configured
        if (openAiApiKey != null && !openAiApiKey.isEmpty()) {
            try {
                return generateWithLlm(naturalLanguage, server);
            } catch (Exception e) {
                log.warn("LLM generation failed, falling back to pattern matching: {}", e.getMessage());
            }
        }

        // Fallback to pattern matching
        return generateWithPattern(lowerInput, server);
    }

    /**
     * Generate command using LLM (OpenAI or Anthropic)
     */
    private AiCommandResult generateWithLlm(String naturalLanguage, Server server) {
        String systemPrompt = buildSystemPrompt(server);
        String userPrompt = buildUserPrompt(naturalLanguage, server);

        // In production, this would call Spring AI
        // For now, return pattern-based result as fallback
        log.info("Using LLM for command generation (mock implementation)");
        return generateWithPattern(naturalLanguage.toLowerCase(), server);
    }

    /**
     * Build system prompt for LLM
     */
    private String buildSystemPrompt(Server server) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Linux server assistant that converts natural language to shell commands.\n");
        prompt.append("Generate ONLY the shell command, nothing else.\n\n");
        prompt.append("Rules:\n");
        prompt.append("1. Return ONLY the command, no explanations\n");
        prompt.append("2. Commands must be safe and read-only by default\n");
        prompt.append("3. For destructive operations, add safety checks\n");
        prompt.append("4. Use full paths for system commands\n\n");

        if (server != null && server.getOsType() != null) {
            prompt.append("OS: ").append(server.getOsType()).append("\n");
            if (server.getOsType().toLowerCase().contains("ubuntu") ||
                server.getOsType().toLowerCase().contains("debian")) {
                prompt.append("Package manager: apt/apt-get\n");
            } else if (server.getOsType().toLowerCase().contains("centos") ||
                       server.getOsType().toLowerCase().contains("rhel")) {
                prompt.append("Package manager: yum/dnf\n");
            }
        }

        return prompt.toString();
    }

    /**
     * Build user prompt for LLM
     */
    private String buildUserPrompt(String naturalLanguage, Server server) {
        return "Convert to Linux command: " + naturalLanguage;
    }

    /**
     * Generate command using pattern matching (fallback)
     */
    private AiCommandResult generateWithPattern(String lowerInput, Server server) {
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
            return new SafetyValidation(false, "Dangerous command detected: " + extractDangerousPattern(lowerCommand), SafetyLevel.HIGH);
        }

        // Commands requiring confirmation
        if (requiresConfirmation(lowerCommand)) {
            return new SafetyValidation(true, "This command requires confirmation: " + extractConfirmationCommand(lowerCommand), SafetyLevel.MEDIUM);
        }

        // Read-only or safe commands
        if (isReadOnlyCommand(command)) {
            return new SafetyValidation(true, "Read-only command", SafetyLevel.LOW);
        }

        return new SafetyValidation(true, "Command appears safe", SafetyLevel.LOW);
    }

    /**
     * Multi-step task decomposition using LLM
     */
    public List<String> decomposeTask(String naturalLanguage, Server server) {
        // Simple task decomposition based on common patterns
        List<String> steps = new ArrayList<>();

        if (naturalLanguage.contains("and")) {
            String[] parts = naturalLanguage.split(" and ");
            for (String part : parts) {
                AiCommandResult result = generateCommand(part, server);
                if (result.getCommand() != null) {
                    steps.add(result.getCommand());
                }
            }
        } else {
            AiCommandResult result = generateCommand(naturalLanguage, server);
            if (result.getCommand() != null) {
                steps.add(result.getCommand());
            }
        }

        return steps;
    }

    /**
     * Risk assessment for command execution
     */
    public RiskAssessment assessRisk(String command, Server server) {
        SafetyValidation validation = validateCommand(command);

        RiskLevel riskLevel;
        if (validation.getLevel() == SafetyLevel.HIGH) {
            riskLevel = RiskLevel.HIGH;
        } else if (validation.getLevel() == SafetyLevel.MEDIUM) {
            riskLevel = RiskLevel.MEDIUM;
        } else if (isReadOnlyCommand(command)) {
            riskLevel = RiskLevel.LOW;
        } else {
            riskLevel = RiskLevel.MEDIUM;
        }

        return new RiskAssessment(
            riskLevel,
            validation.getMessage(),
            riskLevel == RiskLevel.HIGH ? "blocked" : (riskLevel == RiskLevel.MEDIUM ? "warning" : "allowed")
        );
    }

    private boolean containsDangerousPattern(String command) {
        String[] dangerousPatterns = {
            "rm -rf /", "rm -rf /*", "dd if=", ":(){:|:&};:", "> /dev/sda",
            "mkfs.", "fdisk /dev/", "dd of=/dev/", "wget | sh", "curl | sh",
            "chmod -r 777 /", "chmod 000", "shutdown", "reboot -f",
            "init 0", "init 6", "--no-preserve-root", "eval `", "exec `",
            "> /etc/passwd", "> /etc/shadow", "nc -e", "/dev/tcp"
        };

        for (String pattern : dangerousPatterns) {
            if (command.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String extractDangerousPattern(String command) {
        String[] dangerousPatterns = {
            "rm -rf /", "rm -rf /*", "dd if=", ":(){:|:&};:", "> /dev/sda",
            "mkfs.", "fdisk /dev/", "dd of=/dev/", "wget | sh", "curl | sh",
            "chmod -r 777 /", "chmod 000", "shutdown", "reboot -f",
            "init 0", "init 6", "--no-preserve-root", "eval `", "exec `"
        };

        for (String pattern : dangerousPatterns) {
            if (command.contains(pattern)) {
                return pattern;
            }
        }
        return "unknown dangerous pattern";
    }

    private boolean requiresConfirmation(String command) {
        String[] confirmationCommands = {
            "rm -r", "rm -f", "rm --", "shutdown", "reboot",
            "service restart", "systemctl restart", "kill -9", "killall",
            "drop table", "delete from", "truncate", "drop database",
            "reboot", "init 0", "init 6", "poweroff"
        };

        for (String cmd : confirmationCommands) {
            if (command.contains(cmd)) {
                return true;
            }
        }
        return false;
    }

    private String extractConfirmationCommand(String command) {
        String[] confirmationCommands = {
            "rm -r", "rm -f", "shutdown", "reboot",
            "service restart", "systemctl restart", "kill -9", "killall"
        };

        for (String cmd : confirmationCommands) {
            if (command.contains(cmd)) {
                return cmd;
            }
        }
        return "unknown";
    }

    private boolean isLikelyShellCommand(String input) {
        String[] safeCommands = {"ls", "cd", "cat", "grep", "ps", "top", "df", "du", "pwd", "who", "last", "head", "tail", "wc", "find", "awk", "sed", "cut"};
        for (String cmd : safeCommands) {
            if (input.trim().startsWith(cmd)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReadOnlyCommand(String command) {
        String[] readOnlyCommands = {"ls", "cat", "head", "tail", "grep", "wc", "ps", "df", "du", "who", "last", "uptime", "uname", "hostname", "top", "free", "vmstat"};
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
                "system info|show system|server info|os version|linux version|check system",
                "uname -a && cat /etc/os-release",
                "System Information", 0.95, SafetyLevel.LOW
        ));

        // CPU info
        PATTERN_MAPPINGS.add(new PatternMapping(
                "cpu info|show cpu|processor|cpu usage|check cpu",
                "lscpu && top -bn1 | head -20",
                "CPU Information", 0.95, SafetyLevel.LOW
        ));

        // Memory info
        PATTERN_MAPPINGS.add(new PatternMapping(
                "memory info|ram usage|show memory|check memory",
                "free -h && cat /proc/meminfo | head -10",
                "Memory Information", 0.95, SafetyLevel.LOW
        ));

        // Disk usage
        PATTERN_MAPPINGS.add(new PatternMapping(
                "disk usage|disk space|storage|show disk|check disk",
                "df -h && lsblk",
                "Disk Usage", 0.95, SafetyLevel.LOW
        ));

        // Process list
        PATTERN_MAPPINGS.add(new PatternMapping(
                "process list|show process|running processes|check process",
                "ps aux | head -30",
                "Process List", 0.95, SafetyLevel.LOW
        ));

        // Network connections
        PATTERN_MAPPINGS.add(new PatternMapping(
                "network|connections|show netstat|ports|check network",
                "netstat -tulpn | head -30",
                "Network Information", 0.95, SafetyLevel.LOW
        ));

        // Service status
        PATTERN_MAPPINGS.add(new PatternMapping(
                "service status|systemctl|show service|check service",
                "systemctl list-units --type=service --state=running | head -30",
                "Service Status", 0.90, SafetyLevel.LOW
        ));

        // User list
        PATTERN_MAPPINGS.add(new PatternMapping(
                "user list|show users|all users|check users",
                "cat /etc/passwd | grep /bin/bash | cut -d: -f1",
                "User List", 0.90, SafetyLevel.LOW
        ));

        // Docker status
        PATTERN_MAPPINGS.add(new PatternMapping(
                "docker status|docker containers|docker ps|check docker",
                "docker ps -a && docker images",
                "Docker Status", 0.90, SafetyLevel.LOW
        ));

        // Nginx status
        PATTERN_MAPPINGS.add(new PatternMapping(
                "nginx status|nginx process|check nginx",
                "systemctl status nginx && ps aux | grep nginx",
                "Nginx Status", 0.90, SafetyLevel.LOW
        ));

        // Log files
        PATTERN_MAPPINGS.add(new PatternMapping(
                "system log|kernel log|syslog|show log|check log",
                "tail -100 /var/log/syslog",
                "System Logs", 0.90, SafetyLevel.LOW
        ));

        // systemctl start service
        PATTERN_MAPPINGS.add(new PatternMapping(
                "start (\\w+) service|start (\\w+)",
                "systemctl start $1",
                "Service Control", 0.85, SafetyLevel.MEDIUM
        ));

        // systemctl stop service
        PATTERN_MAPPINGS.add(new PatternMapping(
                "stop (\\w+) service|stop (\\w+)",
                "systemctl stop $1",
                "Service Control", 0.85, SafetyLevel.MEDIUM
        ));

        // systemctl restart service
        PATTERN_MAPPINGS.add(new PatternMapping(
                "restart (\\w+) service|restart (\\w+)",
                "systemctl restart $1",
                "Service Control", 0.85, SafetyLevel.MEDIUM
        ));

        // ls directory
        PATTERN_MAPPINGS.add(new PatternMapping(
                "ls |list files|list directory|show files",
                "ls -la",
                "Directory Listing", 0.95, SafetyLevel.LOW
        ));

        // Find file
        PATTERN_MAPPINGS.add(new PatternMapping(
                "find file|search for file|find (\\w+)",
                "find / -name \"$1\" 2>/dev/null | head -20",
                "File Search", 0.90, SafetyLevel.LOW
        ));
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

            if (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String group = matcher.group(i);
                    if (group != null) {
                        result = result.replace("$" + i, group);
                    }
                }
            }

            if (server != null && server.getOsType() != null) {
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
     * Risk assessment result
     */
    @Data
    public static class RiskAssessment {
        private RiskLevel riskLevel;
        private String message;
        private String action; // blocked, warning, allowed

        public RiskAssessment(RiskLevel riskLevel, String message, String action) {
            this.riskLevel = riskLevel;
            this.message = message;
            this.action = action;
        }
    }

    public enum SafetyLevel {
        LOW,      // Read-only, safe
        MEDIUM,   // May modify state, needs confirmation
        WARNING,  // Potentially dangerous
        HIGH      // Dangerous, should not execute
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
}