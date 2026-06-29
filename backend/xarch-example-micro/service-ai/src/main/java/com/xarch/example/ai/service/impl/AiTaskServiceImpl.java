package com.xarch.example.ai.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.ai.entity.AiTask;
import com.xarch.example.ai.mapper.AiTaskMapper;
import com.xarch.example.ai.service.AiTaskService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stub AiTask service — persists task records and exposes CRUD
 * operations. Production code should drive task execution through an
 * async worker pool.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskServiceImpl implements AiTaskService {

    private final AiTaskMapper taskMapper;

    @Override
    public PageResult<AiTask> page(String keyword, String type, Integer status,
                                   int pageNum, int pageSize) {
        try {
            QueryWrapper wrapper = QueryWrapper.create().where("del_flag = 0");
            if (keyword != null && !keyword.isBlank()) {
                wrapper.and("name LIKE ? OR task_code LIKE ?",
                        "%" + keyword + "%", "%" + keyword + "%");
            }
            if (type != null && !type.isBlank()) {
                wrapper.and("type = ?", type);
            }
            if (status != null) {
                wrapper.and("status = ?", status);
            }
            wrapper.orderBy("create_time", false);
            var page = taskMapper.paginate(pageNum, pageSize, wrapper);
            return PageResult.of(page.getRecords(), page.getTotalRow());
        } catch (Exception e) {
            log.warn("AiTaskService.page unavailable: {}", e.getMessage());
            return PageResult.of(List.of(), 0L);
        }
    }

    @Override
    public List<AiTask> listRecent(int limit) {
        try {
            return taskMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where("del_flag = 0")
                            .orderBy("create_time", false)
                            .limit(limit));
        } catch (Exception e) {
            log.warn("AiTaskService.listRecent unavailable: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public AiTask getById(Long id) {
        try {
            return taskMapper.selectOneById(id);
        } catch (Exception e) {
            log.warn("AiTaskService.getById unavailable: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public AiTask getByCode(String code) {
        try {
            return taskMapper.selectOneByQuery(
                    QueryWrapper.create()
                            .where("del_flag = 0 AND task_code = ?", code));
        } catch (Exception e) {
            log.warn("AiTaskService.getByCode unavailable: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public AiTask submit(AiTask task) {
        if (task.getTaskCode() == null || task.getTaskCode().isBlank()) {
            task.setTaskCode(UUID.randomUUID().toString());
        }
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        if (task.getProgress() == null) {
            task.setProgress(0);
        }
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        if (task.getDelFlag() == null) {
            task.setDelFlag(0);
        }
        taskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public boolean cancel(Long id) {
        try {
            AiTask task = taskMapper.selectOneById(id);
            if (task == null) {
                return false;
            }
            if (task.getStatus() != null && (task.getStatus() == 2 || task.getStatus() == 3)) {
                return false;
            }
            task.setStatus(4);
            task.setFinishedAt(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
            return true;
        } catch (Exception e) {
            log.warn("AiTaskService.cancel failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            AiTask task = taskMapper.selectOneById(id);
            if (task == null) {
                return;
            }
            task.setDelFlag(1);
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        } catch (Exception e) {
            log.warn("AiTaskService.delete failed: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> history(Long id) {
        Map<String, Object> snapshot = new HashMap<>();
        try {
            AiTask task = taskMapper.selectOneById(id);
            if (task == null) {
                snapshot.put("error", "Task not found");
                return snapshot;
            }
            snapshot.put("id", task.getId());
            snapshot.put("taskCode", task.getTaskCode());
            snapshot.put("status", task.getStatus());
            snapshot.put("progress", task.getProgress());
            snapshot.put("result", task.getResult());
            snapshot.put("errorMessage", task.getErrorMessage());
            snapshot.put("startedAt", task.getStartedAt());
            snapshot.put("finishedAt", task.getFinishedAt());
        } catch (Exception e) {
            log.warn("AiTaskService.history failed: {}", e.getMessage());
            snapshot.put("error", e.getMessage());
        }
        return snapshot;
    }
}
