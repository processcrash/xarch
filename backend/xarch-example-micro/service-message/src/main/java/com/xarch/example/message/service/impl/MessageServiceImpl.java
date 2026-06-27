package com.xarch.example.message.service.impl;

import com.xarch.example.message.entity.Message;
import com.xarch.example.message.service.MessageService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub MessageService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    @Override public long countUnread(Long userId) { return 0L; }
    @Override public List<Message> listByUser(Long userId) { return List.of(); }
    @Override public List<Message> listByUser(Long userId, String category) { return List.of(); }
    @Override public PageResult<Message> page(String t, int p, int s) { return PageResult.empty(); }
    @Override public Message getById(Long id) { return null; }
    @Override public void create(Message m) { }
    @Override public void update(Message m) { }
    @Override public void delete(Long id) { }
}