package com.xarch.example.system.controller;

import com.xarch.example.system.entity.SysNotice;
import com.xarch.example.system.service.ISysNoticeService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Notice controller — migrated from {@code SysNoticeController}. */
@Tag(name = "Notice")
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final ISysNoticeService noticeService;

    @GetMapping("/list")
    public PageResult<SysNotice> list(SysNotice notice) {
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return PageResult.of(list, list.size());
    }

    @GetMapping(value = "/{noticeId}")
    public ApiResult<SysNotice> getInfo(@PathVariable("noticeId") Long noticeId) {
        return ApiResult.ok(noticeService.selectNoticeById(noticeId));
    }

    @PostMapping
    public ApiResult<Void> add(@RequestBody SysNotice notice) {
        noticeService.insertNotice(notice);
        return ApiResult.ok();
    }

    @PutMapping
    public ApiResult<Void> edit(@RequestBody SysNotice notice) {
        noticeService.updateNotice(notice);
        return ApiResult.ok();
    }

    @DeleteMapping("/{noticeIds}")
    public ApiResult<Void> remove(@PathVariable Long[] noticeIds) {
        noticeService.deleteNoticeByIds(noticeIds);
        return ApiResult.ok();
    }
}