package com.xarch.example.file.controller;

import com.xarch.example.file.entity.TempFile;
import com.xarch.example.file.service.TempFileService;
import com.xarch.starter.core.annotation.Debounce;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.entity.SelectIdsDTO;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** TempFile controller — migrated from monolith. */
@Tag(name = "Temp File")
@RestController
@RequestMapping("/api/temp-files")
@RequiredArgsConstructor
public class TempFileController {

    private final TempFileService tempFileService;

    @GetMapping
    @XarchLog(value = "Query temp file list", type = "QUERY")
    public ApiResult<PageResult<TempFile>> page(
            @RequestParam(required = false) String fileName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(tempFileService.page(fileName, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<TempFile> detail(@PathVariable Long id) {
        return ApiResult.ok(tempFileService.getById(id));
    }

    @PostMapping("/upload")
    @Debounce
    @XarchLog(value = "Upload temp file", type = "CREATE")
    public ApiResult<Map<String, Object>> upload(@RequestPart MultipartFile file) throws IOException {
        TempFile tempFile = tempFileService.uploadFile(file);
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", tempFile.getId());
        result.put("fileName", tempFile.getFileName());
        result.put("filePath", tempFile.getFilePath());
        result.put("fileSize", tempFile.getFileSize());
        return ApiResult.ok(result);
    }

    @PostMapping
    @XarchLog(value = "Create temp file", type = "CREATE")
    public ApiResult<Void> create(@RequestBody TempFile tempFile) {
        tempFileService.create(tempFile);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update temp file", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody TempFile tempFile) {
        tempFile.setId(id);
        tempFileService.update(tempFile);
        return ApiResult.ok();
    }

    @DeleteMapping
    @XarchLog(value = "Delete temp file", type = "DELETE")
    public ApiResult<Void> delete(@RequestBody SelectIdsDTO dto) {
        for (Long id : dto.getIds()) {
            tempFileService.delete(id);
        }
        return ApiResult.ok();
    }
}