package com.xarch.example.ai.service.impl;

import com.xarch.example.ai.entity.ChatMessage;
import com.xarch.example.ai.entity.ChatSession;
import com.xarch.example.ai.mapper.ChatMessageMapper;
import com.xarch.example.ai.mapper.ChatSessionMapper;
import com.xarch.example.ai.service.ChatService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Chat service stub — persists sessions / messages and produces a
 * deterministic echo response so the API is exercisable without a live
 * LLM. Replace {@link #sendMessage(SendMessageRequest)} with a call
 * into Spring AI (OpenAI / Anthropic) once the LLM credentials are
 * configured.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    @Override
    public List<ChatSession> listSessions(Long userId) {
        try {
            return sessionMapper.selectListByQuery(
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .where("user_id = ? AND del_flag = 0", userId)
                            .orderBy("update_time", false));
        } catch (Exception e) {
            log.warn("ChatService.listSessions unavailable: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public PageResult<ChatSession> pageSessions(Long userId, int pageNum, int pageSize) {
        try {
            var wrapper = com.mybatisflex.core.query.QueryWrapper.create()
                    .where("user_id = ? AND del_flag = 0", userId)
                    .orderBy("update_time", false);
            var page = sessionMapper.paginate(pageNum, pageSize, wrapper);
            return PageResult.of(page.getRecords(), page.getTotalRow());
        } catch (Exception e) {
            log.warn("ChatService.pageSessions unavailable: {}", e.getMessage());
            return PageResult.of(List.of(), 0L);
        }
    }

    @Override
    public ChatSession getSession(Long id) {
        try {
            return sessionMapper.selectOneById(id);
        } catch (Exception e) {
            log.warn("ChatService.getSession unavailable: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ChatSession getSessionByExternalId(String sessionId) {
        try {
            return sessionMapper.selectOneByQuery(
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .where("session_id = ? AND del_flag = 0", sessionId));
        } catch (Exception e) {
            log.warn("ChatService.getSessionByExternalId unavailable: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ChatSession createSession(ChatSession session) {
        if (session.getSessionId() == null) {
            session.setSessionId(UUID.randomUUID().toString());
        }
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        if (session.getDelFlag() == null) {
            session.setDelFlag(0);
        }
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public void updateSessionTitle(Long id, String title) {
        try {
            ChatSession session = sessionMapper.selectOneById(id);
            if (session == null) {
                return;
            }
            session.setTitle(title);
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        } catch (Exception e) {
            log.warn("ChatService.updateSessionTitle failed: {}", e.getMessage());
        }
    }

    @Override
    public void deleteSession(Long id) {
        try {
            ChatSession session = sessionMapper.selectOneById(id);
            if (session == null) {
                return;
            }
            session.setDelFlag(1);
            sessionMapper.updateById(session);
        } catch (Exception e) {
            log.warn("ChatService.deleteSession failed: {}", e.getMessage());
        }
    }

    @Override
    public List<ChatMessage> listMessages(String sessionId) {
        try {
            return messageMapper.selectListByQuery(
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .where("session_id = ? AND del_flag = 0", sessionId)
                            .orderBy("create_time", true));
        } catch (Exception e) {
            log.warn("ChatService.listMessages unavailable: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        if (message.getCreateTime() == null) {
            message.setCreateTime(LocalDateTime.now());
        }
        if (message.getDelFlag() == null) {
            message.setDelFlag(0);
        }
        messageMapper.insert(message);
        return message;
    }

    @Override
    public ChatMessage sendMessage(SendMessageRequest request) {
        // Production: route through Spring AI ChatClient here.
        // The current stub returns a deterministic echo so the API surface
        // is exercisable in local dev.
        ChatMessage reply = new ChatMessage();
        reply.setSessionId(request.getSessionId());
        reply.setRole("assistant");
        reply.setModel(request.getModel() != null ? request.getModel() : "stub-model");
        reply.setContent("[stub] " + request.getPrompt());
        return reply;
    }
}
