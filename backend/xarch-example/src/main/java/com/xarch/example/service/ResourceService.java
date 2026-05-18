package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Resource;
import com.xarch.example.mapper.ResourceMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Resource service
 */
@Service
public class ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    private static final String UPLOAD_DIR = "/tmp/xarch-resources/";

    public PageResult<Resource> page(String sceneCode, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Resource>();
        if (sceneCode != null && !sceneCode.isEmpty()) {
            wrapper.like(Resource::getSceneCode, sceneCode);
        }
        wrapper.orderByDesc(Resource::getCreateTime);

        Page<Resource> page = new Page<>(pageNum, pageSize);
        Page<Resource> result = resourceMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public Resource getById(Long id) {
        return resourceMapper.selectById(id);
    }

    public List<Resource> list() {
        return resourceMapper.selectList(null);
    }

    public Resource upload(String sceneCode, String bizKey, MultipartFile file, String[] pathSegments) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectKey = sceneCode + "/" + (bizKey != null ? bizKey : UUID.randomUUID().toString()) + extension;

        Path uploadPath = Paths.get(UPLOAD_DIR + objectKey);
        Files.createDirectories(uploadPath.getParent());
        file.transferTo(uploadPath);

        Resource resource = new Resource();
        resource.setResourceName(originalFilename);
        resource.setObjectKey(objectKey);
        resource.setAccessUrl("/resource/file/" + objectKey);
        resource.setSceneCode(sceneCode);
        resource.setFileSize(file.getSize());
        resource.setFileType(file.getContentType());
        resourceMapper.insert(resource);

        return resource;
    }

    public void create(Resource resource) {
        resourceMapper.insert(resource);
    }

    public void update(Resource resource) {
        resourceMapper.updateById(resource);
    }

    public void delete(Long id) {
        resourceMapper.deleteById(id);
    }
}