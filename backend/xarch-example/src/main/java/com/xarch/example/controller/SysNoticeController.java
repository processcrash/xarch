package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysNotice;
import com.xarch.example.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知公告操作处理
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController {

    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 查询公告列表
     */
    @GetMapping("/list")
    public PageResult<SysNotice> list(SysNotice notice) {
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return PageResult.of(list, list.size());
    }

    /**
     * 获取公告详细信息
     */
    @GetMapping(value = "/{noticeId}")
    public ApiResult<SysNotice> getInfo(@PathVariable("noticeId") Long noticeId) {
        return ApiResult.ok(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增公告
     */
    @PostMapping
    public ApiResult<Void> add(@RequestBody SysNotice notice) {
        noticeService.insertNotice(notice);
        return ApiResult.ok();
    }

    /**
     * 修改公告
     */
    @PutMapping
    public ApiResult<Void> edit(@RequestBody SysNotice notice) {
        noticeService.updateNotice(notice);
        return ApiResult.ok();
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{noticeIds}")
    public ApiResult<Void> remove(@PathVariable Long[] noticeIds) {
        noticeService.deleteNoticeByIds(noticeIds);
        return ApiResult.ok();
    }
}