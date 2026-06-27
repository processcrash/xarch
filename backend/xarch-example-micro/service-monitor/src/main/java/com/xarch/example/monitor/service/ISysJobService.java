package com.xarch.example.monitor.service;

import com.xarch.example.monitor.entity.SysJob;

import java.util.List;

/** Job service contract — Chinese-style methods from monolith. */
public interface ISysJobService {
    List<SysJob> selectJobList(SysJob job);
    SysJob selectJobById(Long jobId);
    void insertJob(SysJob job);
    void updateJob(SysJob job);
    void changeStatus(SysJob job);
    void run(SysJob job);
    void deleteJobByIds(Long[] jobIds);
}