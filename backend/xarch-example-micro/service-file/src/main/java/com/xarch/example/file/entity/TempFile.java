package com.xarch.example.file.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Temp file entity. */
@Data
@Table("xarch_file_temp_file")
public class TempFile implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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