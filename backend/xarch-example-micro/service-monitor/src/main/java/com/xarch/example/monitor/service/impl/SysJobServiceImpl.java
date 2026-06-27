package com.xarch.example.monitor.service.impl;

import com.xarch.example.monitor.entity.SysJob;
import com.xarch.example.monitor.service.ISysJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub job impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl implements ISysJobService {
    @Override public List<SysJob> selectJobList(SysJob j) { return List.of(); }
    @Override public SysJob selectJobById(Long id) { return null; }
    @Override public void insertJob(SysJob j) { }
    @Override public void updateJob(SysJob j) { }
    @Override public void changeStatus(SysJob j) { }
    @Override public void run(SysJob j) { }
    @Override public void deleteJobByIds(Long[] ids) { }
}