package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysNotice;
import com.xarch.example.service.ISysNoticeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysNoticeController unit tests
 */
@SpringBootTest
class SysNoticeControllerTest {

    @Autowired
    private SysNoticeController noticeController;

    @Autowired
    private ISysNoticeService noticeService;

    @Test
    void testList() {
        PageResult<SysNotice> result = noticeController.list(new SysNotice());
        assertNotNull(result);
    }

    @Test
    void testGetInfo() {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle("Test Notice");
        notice.setNoticeType("1");
        notice.setStatus("0");
        noticeService.insertNotice(notice);

        var result = noticeController.getInfo(notice.getNoticeId());
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }
}