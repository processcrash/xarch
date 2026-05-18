package com.xarch.example.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * User Excel import/export model
 */
@Data
public class UserExcel {

    @ExcelProperty("Username")
    @ColumnWidth(15)
    private String username;

    @ExcelProperty("Nickname")
    @ColumnWidth(15)
    private String nickname;

    @ExcelProperty("Email")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("Mobile")
    @ColumnWidth(15)
    private String mobile;

    @ExcelProperty("Status")
    @ColumnWidth(10)
    private String status;

    @ExcelProperty("Department ID")
    @ColumnWidth(15)
    private Long deptId;
}