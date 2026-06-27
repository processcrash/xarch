package com.xarch.example.auth.service.impl;

import com.xarch.example.auth.entity.User;
import com.xarch.example.auth.service.UserService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Stub implementation of {@link UserService}.
 *
 * <p>Methods return {@code null} / empty values — full implementations
 * will be filled in once business logic is decomposed from the monolith.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public PageResult<User> page(String username, String status, int pageNum, int pageSize) {
        log.debug("UserService.page stub called");
        return PageResult.empty();
    }

    @Override
    public User getById(Long id) {
        log.debug("UserService.getById stub called for id={}", id);
        return null;
    }

    @Override
    public void create(User user) {
        log.debug("UserService.create stub called");
    }

    @Override
    public void update(User user) {
        log.debug("UserService.update stub called");
    }

    @Override
    public void delete(Long id) {
        log.debug("UserService.delete stub called");
    }

    @Override
    public List<User> list() {
        log.debug("UserService.list stub called");
        return List.of();
    }

    @Override
    public List<Long> getRoleIds(Long id) {
        log.debug("UserService.getRoleIds stub called");
        return List.of();
    }

    @Override
    public void assignRoles(Long id, List<Long> roleIds) {
        log.debug("UserService.assignRoles stub called");
    }
}