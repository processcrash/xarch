package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Message;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageService unit tests
 */
@XarchTestBase
@DisplayName("MessageService Unit Tests")
class MessageServiceTest {

    private final MessageService messageService;

    MessageServiceTest(MessageService messageService) {
        this.messageService = messageService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<Message> result = messageService.page(null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with type filter")
    void testPageWithType() {
        PageResult<Message> result = messageService.page("1", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("List by user returns messages")
    void testListByUser() {
        List<Message> messages = messageService.listByUser(1L);
        assertNotNull(messages);
    }

    @Test
    @DisplayName("List by user with category returns messages")
    void testListByUserWithCategory() {
        List<Message> messages = messageService.listByUser(1L, "todo");
        assertNotNull(messages);
    }

    @Test
    @DisplayName("Count unread returns non-negative number")
    void testCountUnread() {
        long count = messageService.countUnread(1L);
        assertTrue(count >= 0);
    }

    @Test
    @DisplayName("Get by ID")
    void testGetById() {
        Message message = messageService.getById(1L);
        if (message != null) {
            assertNotNull(message.getTitle());
        }
    }
}
