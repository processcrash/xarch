package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysPost;
import com.xarch.example.service.ISysPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysPostController unit tests
 */
@SpringBootTest
class SysPostControllerTest {

    @Autowired
    private SysPostController postController;

    @Autowired
    private ISysPostService postService;

    @Test
    void testList() {
        PageResult<SysPost> result = postController.list(new SysPost());
        assertNotNull(result);
    }

    @Test
    void testAll() {
        var result = postController.all();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetInfo() {
        SysPost post = new SysPost();
        post.setPostCode("TEST");
        post.setPostName("Test Post");
        post.setPostSort(1);
        post.setStatus("0");
        postService.insertPost(post);

        var result = postController.getInfo(post.getPostId());
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testAdd() {
        SysPost post = new SysPost();
        post.setPostCode("TEST_ADD");
        post.setPostName("Test Add Post");
        post.setPostSort(10);
        post.setStatus("0");

        var result = postController.add(post);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}