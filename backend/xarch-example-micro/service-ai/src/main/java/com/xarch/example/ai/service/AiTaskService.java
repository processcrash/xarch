package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.AiTask;
import com.xarch.starter.core.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * Async AI task service contract.
 */
public interface AiTaskService {

    /**
     * Page through tasks.
     */
    PageResult<AiTask> page(String keyword, String type, Integer status,
                            int pageNum, int pageSize);

    /**
     * List recent tasks (used in dashboards).
     */
    List<AiTask> listRecent(int limit);

    /**
     * Get a task by primary key.
     */
    AiTask getById(Long id);

    /**
     * Get a task by code.
     */
    AiTask getByCode(String code);

    /**
     * Submit a new task.
     */
    AiTask submit(AiTask task);

    /**
     * Cancel a running task.
     */
    boolean cancel(Long id);

    /**
     * Delete a task (logical delete).
     */
    void delete(Long id);

    /**
     * Read a snapshot of the task execution history (result + error
     * message) for the supplied task id.
     */
    Map<String, Object> history(Long id);
}
