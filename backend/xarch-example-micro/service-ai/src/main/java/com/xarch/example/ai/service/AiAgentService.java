package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.Server;

/** AI agent service contract — generates commands from natural language. */
public interface AiAgentService {

    /**
     * Translate a natural-language instruction into a Linux command.
     */
    AiCommandResult generateCommand(String naturalLanguage, Server server);

    /**
     * Validate a command against safety rules.
     */
    SafetyValidation validateCommand(String command);

    /** Result of command generation. */
    final class AiCommandResult {
        private String command;
        private String category;
        private double confidence;
        private SafetyLevel safetyLevel;

        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public SafetyLevel getSafetyLevel() { return safetyLevel; }
        public void setSafetyLevel(SafetyLevel safetyLevel) { this.safetyLevel = safetyLevel; }
    }

    /** Safety validation result. */
    final class SafetyValidation {
        private boolean safe;
        private String message;
        private SafetyLevel level;

        public boolean isSafe() { return safe; }
        public void setSafe(boolean safe) { this.safe = safe; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public SafetyLevel getLevel() { return level; }
        public void setLevel(SafetyLevel level) { this.level = level; }
    }

    /** Risk levels reported by the validator. */
    enum SafetyLevel { LOW, MEDIUM, HIGH }
}