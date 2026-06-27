package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.LoginLog;
import com.xarch.example.system.service.LoginLogService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Stub LoginLogService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {
    @Override public PageResult<LoginLog> page(String u, int p, int s) { return PageResult.empty(); }
}