package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.OpLog;
import com.xarch.example.system.service.OpLogService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Stub OpLogService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpLogServiceImpl implements OpLogService {
    @Override public PageResult<OpLog> page(String u, int p, int s) { return PageResult.empty(); }
}