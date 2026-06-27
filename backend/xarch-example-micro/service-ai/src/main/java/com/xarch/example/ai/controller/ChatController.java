package com.xarch.example.ai.controller;

import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Chat controller — LLM chat sessions.
 *
 * <p><b>Status: planned.</b> Endpoint paths and request/response shapes
 * are placeholders pending integration with the chosen LLM provider.
 */
@Tag(name = "AI Chat", description = "LLM chat endpoints (planned)")
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class ChatController {

    /**
     * Send a chat prompt.
     */
    @PostMapping("/send")
    public ApiResult<ChatResponse> send(@RequestBody ChatRequest request) {
        return ApiResult.success(new ChatResponse("echo: " + request.getPrompt()));
    }

    /**
     * List recent chat sessions for the current user.
     */
    @GetMapping("/sessions")
    public ApiResult<List<ChatSessionVO>> sessions() {
        return ApiResult.success(List.of());
    }

    /** Chat request payload. */
    @Data
    public static class ChatRequest {
        private String sessionId;
        private String prompt;
        private String model;
    }

    /** Chat response payload. */
    @Data
    public static class ChatResponse {
        private final String reply;
        public ChatResponse(String reply) { this.reply = reply; }
    }

    /** Chat session view object. */
    @Data
    public static class ChatSessionVO {
        private String sessionId;
        private String title;
        private String model;
    }
}