package com.xarch.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Import batch entity for tracking import operations
 */
@Data
@TableName("sys_import_batch")
public class ImportBatch implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchNo;

    private String tableName;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private Integer status;

    private String errorFilePath;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}