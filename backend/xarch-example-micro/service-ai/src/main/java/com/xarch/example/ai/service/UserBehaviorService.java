package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.UserBehavior;
import com.xarch.starter.core.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * AI user-behaviour service contract.
 */
public interface UserBehaviorService {

    /**
     * Record a behaviour event.
     */
    UserBehavior record(UserBehavior behavior);

    /**
     * Page through behaviour events.
     */
    PageResult<UserBehavior> page(Long userId, String action,
                                  int pageNum, int pageSize);

    /**
     * Aggregate behaviour statistics for a user (or every user when
     * {@code userId} is {@code null}).
     */
    Map<String, Object> statistics(Long userId);

    /**
     * Recent behaviour for a user.
     */
    List<UserBehavior> recentByUser(Long userId, int limit);
}
