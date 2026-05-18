package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Message;
import com.xarch.example.mapper.MessageMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Message service
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public PageResult<Message> page(String msgType, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>();
        if (msgType != null && !msgType.isEmpty()) {
            wrapper.eq(Message::getMsgType, msgType);
        }
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> page = new Page<>(pageNum, pageSize);
        Page<Message> result = messageMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public Message getById(Long id) {
        return messageMapper.selectById(id);
    }

    public List<Message> listByUser(Long userId, String category) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>();
        wrapper.eq(Message::getSenderId, userId);
        if ("todo".equals(category)) {
            wrapper.eq(Message::getIsRead, 0);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    public List<Message> listByUser(Long userId) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>();
        wrapper.eq(Message::getSenderId, userId);
        wrapper.orderByDesc(Message::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    public long countUnread(Long userId) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>();
        wrapper.eq(Message::getSenderId, userId).eq(Message::getIsRead, 0);
        return messageMapper.selectCount(wrapper);
    }

    public void create(Message message) {
        messageMapper.insert(message);
    }

    public void update(Message message) {
        messageMapper.updateById(message);
    }

    public void delete(Long id) {
        messageMapper.deleteById(id);
    }
}