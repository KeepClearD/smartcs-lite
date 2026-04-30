package com.smartcs.lite.controller;

import com.smartcs.lite.common.Result;
import com.smartcs.lite.interceptor.TenantContext;
import com.smartcs.lite.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {
        String key = storageService.generateKey(
                TenantContext.get(), "uploads", file.getOriginalFilename());
        storageService.upload(key, file.getInputStream(),
                file.getContentType(), file.getSize());
        return Result.ok(Map.of("key", key, "name", file.getOriginalFilename()));
    }
}
