package com.xarch.example.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.TempFile;
import com.xarch.example.mapper.TempFileMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Temp file service
 */
@Service
public class TempFileService {

    @Autowired
    private TempFileMapper tempFileMapper;

    private static final String UPLOAD_DIR = "/tmp/xarch-temp-files/";

    public PageResult<TempFile> page(String fileName, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_temp_file").where("del_flag = 0");
        if (StringUtils.hasText(fileName)) {
            wrapper.and("file_name LIKE ?", "%" + fileName + "%");
        }
        wrapper.orderBy("create_time", false);

        Page<TempFile> page = tempFileMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public TempFile getById(Long id) {
        return tempFileMapper.selectById(id);
    }

    public TempFile uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filePath = UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get(UPLOAD_DIR + filePath);
        Files.createDirectories(uploadPath.getParent());
        file.transferTo(uploadPath);

        TempFile tempFile = new TempFile();
        tempFile.setFileName(originalFilename);
        tempFile.setFilePath(filePath);
        tempFile.setFileSize(file.getSize());
        tempFile.setFileType(file.getContentType());
        tempFileMapper.insert(tempFile);

        return tempFile;
    }

    public void create(TempFile tempFile) {
        tempFileMapper.insert(tempFile);
    }

    public void update(TempFile tempFile) {
        tempFileMapper.updateById(tempFile);
    }

    public void delete(Long id) {
        tempFileMapper.deleteById(id);
    }
}