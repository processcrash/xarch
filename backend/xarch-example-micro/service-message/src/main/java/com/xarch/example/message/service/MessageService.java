package com.xarch.example.message.service;

import com.xarch.example.message.entity.Message;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/** Message service contract. */
public interface MessageService {
    long countUnread(Long userId);
    List<Message> listByUser(Long userId);
    List<Message> listByUser(Long userId, String category);
    PageResult<Message> page(String msgType, int pageNum, int pageSize);
    Message getById(Long id);
    void create(Message message);
    void update(Message message);
    void delete(Long id);
}