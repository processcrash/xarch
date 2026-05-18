package com.xarch.example.service;

import com.xarch.example.entity.User;
import com.xarch.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User service
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User getById(Long id) {
        return userMapper.findById(id);
    }

    public User getByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public List<User> listAll() {
        return userMapper.findAll();
    }

    public boolean save(User user) {
        return userMapper.insert(user) > 0;
    }

    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }

    public boolean delete(Long id) {
        return userMapper.deleteById(id) > 0;
    }
}