package com.xarch.example.mapper;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 通知公告表 数据层
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
    /**
     * 查询公告信息
     */
    SysNotice selectNoticeById(Long noticeId);

    /**
     * 查询公告列表
     */
    List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     */
    int insertNotice(SysNotice notice);

    /**
     * 修改公告
     */
    int updateNotice(SysNotice notice);

    /**
     * 删除公告
     */
    int deleteNoticeById(Long noticeId);

    /**
     * 批量删除公告
     */
    int deleteNoticeByIds(Long[] noticeIds);
}