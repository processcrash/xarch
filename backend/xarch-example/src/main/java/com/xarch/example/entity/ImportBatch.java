package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Import batch entity for tracking import operations
 */
@Data
@Table("sys_import_batch")
public class ImportBatch implements Serializable {

    @Id(auto = true)
    private Long id;

    private String batchNo;

    private String tableName;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private Integer status;

    private String errorFilePath;

    private Long createUserId;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;
}