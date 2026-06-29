package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.ChatMessage;
import com.xarch.example.ai.entity.ChatSession;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/**
 * Chat service contract — session lifecycle and message history.
 *
 * <p>Wires the AI chat feature on top of {@link ChatSession} /
 * {@link ChatMessage} persistence and a Spring-AI model client. Methods
 * are designed to be safely callable when the LLM backend is absent
 * (e.g. local development) — callers should still handle empty results.</p>
 */
public interface ChatService {

    /**
     * List recent chat sessions for a user (most recently updated first).
     *
     * @param userId owning user id
     * @return ordered list of sessions, may be empty
     */
    List<ChatSession> listSessions(Long userId);

    /**
     * Page through chat sessions.
     */
    PageResult<ChatSession> pageSessions(Long userId, int pageNum, int pageSize);

    /**
     * Get a chat session by primary key.
     */
    ChatSession getSession(Long id);

    /**
     * Get a chat session by external sessionId.
     */
    ChatSession getSessionByExternalId(String sessionId);

    /**
     * Create a new chat session.
     */
    ChatSession createSession(ChatSession session);

    /**
     * Update the title of an existing session.
     */
    void updateSessionTitle(Long id, String title);

    /**
     * Delete (logically) a chat session and all its messages.
     */
    void deleteSession(Long id);

    /**
     * List messages belonging to a session, oldest first.
     */
    List<ChatMessage> listMessages(String sessionId);

    /**
     * Persist a message (user or assistant).
     */
    ChatMessage saveMessage(ChatMessage message);

    /**
     * Send a user prompt and return the assistant reply.
     *
     * <p>Production wiring: routes through Spring AI
     * (OpenAI / Anthropic) to produce a real response. The current
     * implementation returns a deterministic echo when no LLM is
     * configured so the API surface stays usable in local dev.</p>
     */
    ChatMessage sendMessage(SendMessageRequest request);

    /** Bundle for the {@link #sendMessage(SendMessageRequest)} call. */
    final class SendMessageRequest {
        private String sessionId;
        private Long userId;
        private String prompt;
        private String model;
        private boolean newSession;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public boolean isNewSession() { return newSession; }
        public void setNewSession(boolean newSession) { this.newSession = newSession; }
    }
}
