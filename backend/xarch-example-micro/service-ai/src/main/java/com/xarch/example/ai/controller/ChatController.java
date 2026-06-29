package com.xarch.example.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.ai.entity.ChatMessage;
import com.xarch.example.ai.entity.ChatSession;
import com.xarch.example.ai.service.ChatService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI chat controller — manages LLM chat sessions and message history.
 *
 * <p>Designed to wire into Spring AI (OpenAI / Anthropic) in production.
 * The current implementation persists sessions / messages through
 * {@link ChatService} and returns a deterministic echo reply when no
 * LLM is configured, so the API surface stays usable in local dev.</p>
 */
@Slf4j
@Tag(name = "AI Chat", description = "AI chat sessions and message history")
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * List chat sessions for the current user, most recently updated first.
     */
    @GetMapping("/sessions")
    @Operation(summary = "List chat sessions for the current user")
    public ApiResult<List<ChatSession>> listSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(chatService.listSessions(userId));
    }

    /**
     * Page through chat sessions for the current user.
     */
    @GetMapping("/sessions/page")
    @Operation(summary = "Page chat sessions for the current user")
    public ApiResult<PageResult<ChatSession>> pageSessions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(chatService.pageSessions(userId, pageNum, pageSize));
    }

    /**
     * Get a single chat session by primary key.
     */
    @GetMapping("/sessions/{id}")
    @Operation(summary = "Get chat session detail")
    public ApiResult<ChatSession> getSession(@PathVariable Long id) {
        ChatSession session = chatService.getSession(id);
        if (session == null) {
            return ApiResult.fail("Chat session not found");
        }
        return ApiResult.success(session);
    }

    /**
     * Create a new chat session.
     */
    @PostMapping("/sessions")
    @XarchLog(value = "Create chat session", type = "CREATE")
    @Operation(summary = "Create a new chat session")
    public ApiResult<ChatSession> createSession(@Valid @RequestBody SessionRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ChatSession session = new ChatSession();
        session.setSessionId(request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle(request.getTitle() != null ? request.getTitle() : "New chat");
        session.setModel(request.getModel());
        return ApiResult.success(chatService.createSession(session));
    }

    /**
     * Update the title of an existing session.
     */
    @PutMapping("/sessions/{id}/title")
    @XarchLog(value = "Update chat session title", type = "UPDATE")
    @Operation(summary = "Update chat session title")
    public ApiResult<Void> updateSessionTitle(@PathVariable Long id, @RequestParam String title) {
        chatService.updateSessionTitle(id, title);
        return ApiResult.success(null);
    }

    /**
     * Delete a chat session and all of its messages.
     */
    @DeleteMapping("/sessions/{id}")
    @XarchLog(value = "Delete chat session", type = "DELETE")
    @Operation(summary = "Delete a chat session")
    public ApiResult<Void> deleteSession(@PathVariable Long id) {
        chatService.deleteSession(id);
        return ApiResult.success(null);
    }

    /**
     * List messages belonging to a session.
     */
    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "List messages for a chat session")
    public ApiResult<List<ChatMessage>> listMessages(@PathVariable String sessionId) {
        return ApiResult.success(chatService.listMessages(sessionId));
    }

    /**
     * Send a user message and return the assistant reply.
     *
     * <p>Production wiring: routes through Spring AI ChatClient. The
     * current stub returns an echo so the API is exercisable without
     * a live LLM.</p>
     */
    @PostMapping("/send")
    @XarchLog(value = "Send chat message", type = "OPERATION")
    @Operation(summary = "Send a user message and receive the assistant reply")
    public ApiResult<ChatMessage> send(@Valid @RequestBody SendRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            ChatSession session = chatService.createSession(buildSession(userId, request.getModel()));
            sessionId = session.getSessionId();
        } else if (chatService.getSessionByExternalId(sessionId) == null) {
            chatService.createSession(buildSession(userId, request.getModel()));
        }

        // Persist the user turn
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(request.getPrompt());
        userMsg.setModel(request.getModel());
        userMsg.setCreateUserId(userId);
        userMsg.setCreateTime(LocalDateTime.now());
        userMsg.setDelFlag(0);
        chatService.saveMessage(userMsg);

        // Produce the assistant turn
        ChatService.SendMessageRequest svcRequest = new ChatService.SendMessageRequest();
        svcRequest.setSessionId(sessionId);
        svcRequest.setUserId(userId);
        svcRequest.setPrompt(request.getPrompt());
        svcRequest.setModel(request.getModel());
        ChatMessage reply = chatService.sendMessage(svcRequest);
        chatService.saveMessage(reply);
        return ApiResult.success(reply);
    }

    private ChatSession buildSession(Long userId, String model) {
        ChatSession session = new ChatSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle("New chat");
        session.setModel(model);
        return session;
    }

    /** Create-session request payload. */
    @Data
    public static class SessionRequest {
        private String sessionId;
        @NotBlank
        private String title;
        private String model;
    }

    /** Send-message request payload. */
    @Data
    public static class SendRequest {
        private String sessionId;
        @NotBlank
        private String prompt;
        private String model;
    }
}
