package com.xarch.example.message.controller;

import com.xarch.example.message.entity.Message;
import com.xarch.example.message.service.MessageService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Message controller — migrated from monolith. */
@Tag(name = "Message")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/count")
    public ApiResult<Map<String, Object>> countMyMessage() {
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", messageService.countUnread(1L));
        return ApiResult.ok(result);
    }

    @GetMapping("/list/todo")
    public ApiResult<List<Message>> listTodo() {
        return ApiResult.ok(messageService.listByUser(1L, "todo"));
    }

    @GetMapping("/list/msg")
    public ApiResult<List<Message>> listMsg() {
        return ApiResult.ok(messageService.listByUser(1L));
    }

    @GetMapping
    @XarchLog(value = "Query message list", type = "QUERY")
    public ApiResult<PageResult<Message>> page(
            @RequestParam(required = false) String msgType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(messageService.page(msgType, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Message> detail(@PathVariable Long id) {
        return ApiResult.ok(messageService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create message", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Message message) {
        messageService.create(message);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update message", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Message message) {
        message.setId(id);
        messageService.update(message);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete message", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        messageService.delete(id);
        return ApiResult.ok();
    }
}