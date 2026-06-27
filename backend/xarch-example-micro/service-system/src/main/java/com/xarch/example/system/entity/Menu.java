package com.xarch.example.system.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Menu entity owned by {@code service-system}.
 */
@Data
@Table("xarch_system_menu")
public class Menu implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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