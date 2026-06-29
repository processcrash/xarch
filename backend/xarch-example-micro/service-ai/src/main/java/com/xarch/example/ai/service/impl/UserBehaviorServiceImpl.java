package com.xarch.example.ai.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.ai.entity.UserBehavior;
import com.xarch.example.ai.mapper.UserBehaviorMapper;
import com.xarch.example.ai.service.UserBehaviorService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserBehaviour service — records and aggregates AI user actions.
 *
 * <p>Backed by {@link UserBehaviorMapper} (MyBatis-Flex). Statistics
 * are computed in Java for the scaffold; production code should push
 * these into ClickHouse / Elasticsearch for scale.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorServiceImpl implements UserBehaviorService {

    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    @Transactional
    public UserBehavior record(UserBehavior behavior) {
        if (behavior.getCreateTime() == null) {
            behavior.setCreateTime(LocalDateTime.now());
        }
        userBehaviorMapper.insert(behavior);
        return behavior;
    }

    @Override
    public PageResult<UserBehavior> page(Long userId, String action, int pageNum, int pageSize) {
        try {
            QueryWrapper wrapper = QueryWrapper.create();
            if (userId != null) {
                wrapper.where("user_id = ?", userId);
            }
            if (action != null && !action.isBlank()) {
                wrapper.and("action = ?", action);
            }
            wrapper.orderBy("create_time", false);
            var page = userBehaviorMapper.paginate(pageNum, pageSize, wrapper);
            return PageResult.of(page.getRecords(), page.getTotalRow());
        } catch (Exception e) {
            log.warn("UserBehaviorService.page unavailable: {}", e.getMessage());
            return PageResult.of(List.of(), 0L);
        }
    }

    @Override
    public Map<String, Object> statistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        try {
            QueryWrapper wrapper = QueryWrapper.create();
            if (userId != null) {
                wrapper.where("user_id = ?", userId);
            }
            List<UserBehavior> events = userBehaviorMapper.selectListByQuery(wrapper);
            stats.put("total", events.size());
            Map<String, Integer> actionCounts = new HashMap<>();
            for (UserBehavior event : events) {
                actionCounts.merge(event.getAction(), 1, Integer::sum);
            }
            stats.put("byAction", actionCounts);
            stats.put("userId", userId);
        } catch (Exception e) {
            log.warn("UserBehaviorService.statistics unavailable: {}", e.getMessage());
            stats.put("total", 0);
            stats.put("byAction", Map.of());
        }
        return stats;
    }

    @Override
    public List<UserBehavior> recentByUser(Long userId, int limit) {
        try {
            return userBehaviorMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where("user_id = ?", userId)
                            .orderBy("create_time", false)
                            .limit(limit));
        } catch (Exception e) {
            log.warn("UserBehaviorService.recentByUser unavailable: {}", e.getMessage());
            return List.of();
        }
    }
}
