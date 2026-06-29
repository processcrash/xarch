package com.xarch.example.ai.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.ai.entity.McpServer;
import com.xarch.example.ai.mapper.McpServerMapper;
import com.xarch.example.ai.service.McpServerService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Stub McpServer service — persists registry entries and exposes a
 * minimal health-check probe. Production wiring should plug in an MCP
 * client SDK (e.g. Java MCP SDK) and probe via SSE / stdio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpServerServiceImpl implements McpServerService {

    private final McpServerMapper mcpServerMapper;

    @Override
    public PageResult<McpServer> page(String keyword, String tag, Integer healthStatus,
                                      int pageNum, int pageSize) {
        try {
            QueryWrapper wrapper = QueryWrapper.create()
                    .where("del_flag = 0");
            if (keyword != null && !keyword.isBlank()) {
                wrapper.and("name LIKE ? OR base_url LIKE ?", "%" + keyword + "%", "%" + keyword + "%");
            }
            if (tag != null && !tag.isBlank()) {
                wrapper.and("tags LIKE ?", "%" + tag + "%");
            }
            if (healthStatus != null) {
                wrapper.and("health_status = ?", healthStatus);
            }
            wrapper.orderBy("create_time", false);
            var page = mcpServerMapper.paginate(pageNum, pageSize, wrapper);
            return PageResult.of(page.getRecords(), page.getTotalRow());
        } catch (Exception e) {
            log.warn("McpServerService.page unavailable: {}", e.getMessage());
            return PageResult.of(List.of(), 0L);
        }
    }

    @Override
    public List<McpServer> listAll() {
        try {
            return mcpServerMapper.selectListByQuery(
                    QueryWrapper.create().where("del_flag = 0")
                            .orderBy("name", true));
        } catch (Exception e) {
            log.warn("McpServerService.listAll unavailable: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public McpServer getById(Long id) {
        try {
            return mcpServerMapper.selectOneById(id);
        } catch (Exception e) {
            log.warn("McpServerService.getById unavailable: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public McpServer register(McpServer server) {
        server.setCreateTime(LocalDateTime.now());
        server.setUpdateTime(LocalDateTime.now());
        if (server.getDelFlag() == null) {
            server.setDelFlag(0);
        }
        if (server.getHealthStatus() == null) {
            server.setHealthStatus(0);
        }
        mcpServerMapper.insert(server);
        return server;
    }

    @Override
    @Transactional
    public McpServer update(McpServer server) {
        server.setUpdateTime(LocalDateTime.now());
        mcpServerMapper.updateById(server);
        return server;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        try {
            McpServer server = mcpServerMapper.selectOneById(id);
            if (server == null) {
                return;
            }
            server.setDelFlag(1);
            server.setUpdateTime(LocalDateTime.now());
            mcpServerMapper.updateById(server);
        } catch (Exception e) {
            log.warn("McpServerService.remove failed: {}", e.getMessage());
        }
    }

    @Override
    public boolean healthCheck(Long id) {
        try {
            McpServer server = mcpServerMapper.selectOneById(id);
            if (server == null) {
                return false;
            }
            // Stub: production code should perform an MCP handshake here.
            // For the scaffold we treat every server as healthy.
            server.setHealthStatus(1);
            server.setLastHealthCheck(LocalDateTime.now());
            server.setLastError(null);
            mcpServerMapper.updateById(server);
            return true;
        } catch (Exception e) {
            log.warn("McpServerService.healthCheck failed: {}", e.getMessage());
            return false;
        }
    }
}
