package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Client;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientService unit tests
 */
@XarchTestBase
@DisplayName("ClientService Unit Tests")
class ClientServiceTest {

    private final ClientService clientService;

    ClientServiceTest(ClientService clientService) {
        this.clientService = clientService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<Client> result = clientService.page(null, null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with name filter")
    void testPageWithName() {
        PageResult<Client> result = clientService.page("test", null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("List returns clients")
    void testList() {
        List<Client> clients = clientService.list();
        assertNotNull(clients);
    }

    @Test
    @DisplayName("Get by ID")
    void testGetById() {
        Client client = clientService.getById(1L);
        if (client != null) {
            assertNotNull(client.getClientId());
        }
    }
}
