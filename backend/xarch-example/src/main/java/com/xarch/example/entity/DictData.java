package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Dictionary data entity
 */
@Data
@Table("sys_dict_data")
public class DictData implements Serializable {

    @Id(auto = true)
    private Long id;

    private Long dictId;

    private String dictLabel;

    private String dictValue;

    private Integer sortOrder;

    private Integer status;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}