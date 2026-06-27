package com.xarch.example.ai.service.impl;

import com.xarch.example.ai.entity.CommandHistory;
import com.xarch.example.ai.entity.Server;
import com.xarch.example.ai.service.ServerManageService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Stub ServerManageService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerManageServiceImpl implements ServerManageService {
    @Override public PageResult<Server> page(String k, String g, Integer s, int p, int sz) { return PageResult.empty(); }
    @Override public Server getById(Long id) { return null; }
    @Override public void create(Server s) { }
    @Override public void update(Server s) { }
    @Override public void delete(Long id) { }
    @Override public boolean connect(Long id) { return false; }
    @Override public void disconnect(Long id) { }
    @Override public boolean testConnection(Server s) { return false; }
    @Override public String importPrivateKey(MultipartFile f) throws IOException { return ""; }
    @Override public CommandHistory executeCommand(CommandRequest r) { return null; }
    @Override public CommandHistory executeAiCommand(Long id, String n, String sid) { return null; }
    @Override public PageResult<CommandHistory> getCommandHistory(Long id, String sid, int p, int s) { return PageResult.empty(); }
    @Override public CommandHistory getHistoryById(Long id) { return null; }
}