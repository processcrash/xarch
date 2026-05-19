package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Temp file entity for template management
 */
@Data
@Table("sys_temp_file")
public class TempFile implements Serializable {

    @Id(auto = true)
    private Long id;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Integer status;

    private String description;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}