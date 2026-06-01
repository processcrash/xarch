package com.xarch.example.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.Message;
import com.xarch.example.mapper.MessageMapper;
import com.xarch.starter.core.result.PageResult;
import com.mybatisflex.core.paginate.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Message service
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public PageResult<Message> page(String msgType, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_message").where("del_flag = 0");
        if (StringUtils.hasText(msgType)) {
            wrapper.and("msg_type = ?", msgType);
        }
        wrapper.orderBy("create_time", false);

        Page<Message> page = messageMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public Message getById(Long id) {
        return messageMapper.selectById(id);
    }

    public List<Message> listByUser(Long userId, String category) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_message")
                .where("sender_id = ?", userId);
        if ("todo".equals(category)) {
            wrapper.and("is_read = 0");
        }
        wrapper.orderBy("create_time", false);
        return messageMapper.selectListByQuery(wrapper);
    }

    public List<Message> listByUser(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_message")
                .where("sender_id = ?", userId)
                .orderBy("create_time", false);
        return messageMapper.selectListByQuery(wrapper);
    }

    public long countUnread(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_message")
                .where("sender_id = ?", userId)
                .and("is_read = 0");
        return messageMapper.selectCountByQuery(wrapper);
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