package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;

/**
 * 岗位表 sys_post
 */
@Data
@Table("sys_post")
public class SysPost {

    /** 岗位序号 */
    @Id(auto = true)
    private Long postId;

    /** 岗位编码 */
    private String postCode;

    /** 岗位名称 */
    private String postName;

    /** 岗位排序 */
    private Integer postSort;

    /** 状态（0正常 1停用） */
    private String status;
}