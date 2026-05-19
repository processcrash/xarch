package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Menu entity
 */
@Data
@Table("sys_menu")
public class Menu implements Serializable {

    @Id(auto = true)
    private Long id;

    private Long parentId;

    private String menuName;

    private String menuCode;

    private Integer menuType;

    private String path;

    private String icon;

    private Integer sortOrder;

    private Integer status;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;

    private transient java.util.List<Menu> children;
}