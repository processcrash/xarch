package com.xarch.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Temp file entity for template management
 */
@Data
@TableName("sys_temp_file")
public class TempFile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Integer status;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer delFlag;
}