package com.xarch.example.service.impl;

import com.xarch.example.entity.User;
import com.xarch.example.excel.UserExcel;
import com.xarch.example.mapper.UserMapper;
import com.xarch.example.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel import/export service implementation
 */
@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserExcel> exportUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(this::convertToExcel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importUsers(MultipartFile file) {
        try {
            List<UserExcel> excelUsers = com.alibaba.excel.EasyExcel.read(file.getInputStream())
                    .head(UserExcel.class)
                    .sheet()
                    .doReadSync();

            int count = 0;
            for (UserExcel excelUser : excelUsers) {
                User user = convertFromExcel(excelUser);
                userMapper.insert(user);
                count++;
            }
            return count;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }
    }

    private UserExcel convertToExcel(User user) {
        UserExcel excel = new UserExcel();
        excel.setUsername(user.getUsername());
        excel.setNickname(user.getNickname());
        excel.setEmail(user.getEmail());
        excel.setMobile(user.getMobile());
        excel.setStatus(user.getStatus() != null && user.getStatus() == 1 ? "Disabled" : "Normal");
        excel.setDeptId(user.getDeptId());
        return excel;
    }

    private User convertFromExcel(UserExcel excel) {
        User user = new User();
        user.setUsername(excel.getUsername());
        user.setNickname(excel.getNickname());
        user.setEmail(excel.getEmail());
        user.setMobile(excel.getMobile());
        user.setStatus("Disabled".equals(excel.getStatus()) ? 1 : 0);
        user.setDeptId(excel.getDeptId());
        user.setPassword("admin123"); // Default password
        return user;
    }
}