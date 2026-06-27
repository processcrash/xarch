package com.xarch.example.system.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Dictionary data entity owned by {@code service-system}.
 */
@Data
@Table("xarch_system_dict_data")
public class DictData implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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