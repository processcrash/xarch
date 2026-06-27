package com.xarch.example.ai.service.impl;

import com.xarch.example.ai.entity.Server;
import com.xarch.example.ai.service.AiAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Stub AI agent service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentServiceImpl implements AiAgentService {
    @Override public AiCommandResult generateCommand(String n, Server s) { return new AiCommandResult(); }
    @Override public SafetyValidation validateCommand(String c) { return new SafetyValidation(); }
}