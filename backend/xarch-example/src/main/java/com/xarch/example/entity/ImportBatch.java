package com.xarch.example.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Import batch entity for tracking import operations
 */
@Data
@Table("sys_import_batch")
public class ImportBatch implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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