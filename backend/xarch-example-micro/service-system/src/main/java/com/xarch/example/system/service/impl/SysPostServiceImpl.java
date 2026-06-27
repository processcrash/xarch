package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.SysPost;
import com.xarch.example.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub post impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl implements ISysPostService {
    @Override public List<SysPost> selectPostList(SysPost p) { return List.of(); }
    @Override public List<SysPost> selectPostAll() { return List.of(); }
    @Override public SysPost selectPostById(Long id) { return null; }
    @Override public void insertPost(SysPost p) { }
    @Override public void updatePost(SysPost p) { }
    @Override public void deletePostByIds(Long[] ids) { }
}