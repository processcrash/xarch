package com.xarch.example.service.impl;

import com.xarch.example.entity.SysJob;
import com.xarch.example.mapper.SysJobMapper;
import com.xarch.example.service.ISysJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定时任务调度信息 服务层
 */
@Service
public class SysJobServiceImpl implements ISysJobService {
    @Autowired
    private SysJobMapper jobMapper;

    @Override
    public List<SysJob> selectJobList(SysJob job) {
        return jobMapper.selectJobList(job);
    }

    @Override
    public SysJob selectJobById(Long jobId) {
        return jobMapper.selectJobById(jobId);
    }

    @Override
    @Transactional
    public int pauseJob(SysJob job) {
        job.setStatus("1");
        return jobMapper.updateJob(job);
    }

    @Override
    @Transactional
    public int resumeJob(SysJob job) {
        job.setStatus("0");
        return jobMapper.updateJob(job);
    }

    @Override
    @Transactional
    public int deleteJob(SysJob job) {
        return jobMapper.deleteJobById(job.getJobId());
    }

    @Override
    @Transactional
    public void deleteJobByIds(Long[] jobIds) {
        for (Long jobId : jobIds) {
            jobMapper.deleteJobById(jobId);
        }
    }

    @Override
    @Transactional
    public int changeStatus(SysJob job) {
        if ("0".equals(job.getStatus())) {
            return resumeJob(job);
        } else {
            return pauseJob(job);
        }
    }

    @Override
    @Transactional
    public boolean run(SysJob job) {
        return true;
    }

    @Override
    @Transactional
    public int insertJob(SysJob job) {
        job.setStatus("1");
        return jobMapper.insertJob(job);
    }

    @Override
    @Transactional
    public int updateJob(SysJob job) {
        return jobMapper.updateJob(job);
    }

    @Override
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return cronExpression != null && !cronExpression.isEmpty();
    }
}