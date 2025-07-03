package com.eduneu.web1.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${file.upload.path:src/main/resources/image}")
    private String uploadPath;

    @Value("${file.max.size:10485760}") // 默认10MB
    private long maxFileSize;

    private static final String[] ALLOWED_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    @PostMapping("/api/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        // 验证文件是否存在
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择文件");
        }

        // 验证文件类型
        if (!isValidFileType(file.getContentType())) {
            return ResponseEntity.badRequest().body("不支持的文件类型");
        }

        // 验证文件大小
        if (file.getSize() > maxFileSize) {
            return ResponseEntity.badRequest().body("文件大小超过限制");
        }

        try {
            // 确保上传目录存在
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建上传目录: {}", uploadDir);
            }

            // 安全地获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID() + fileExtension;
            Path filePath = uploadDir.resolve(newFilename);

            // 保存文件
            file.transferTo(filePath);
            log.info("文件上传成功: {}", filePath);

            // 返回文件访问URL
            Map<String, String> response = new HashMap<>();
            response.put("url", "/uploads/" + newFilename);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("文件上传失败");
        } catch (Exception e) {
            log.error("文件处理错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("文件处理错误");
        }
    }

    private boolean isValidFileType(String contentType) {
        if (contentType == null) return false;

        for (String allowedType : ALLOWED_TYPES) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }

        log.warn("不支持的文件类型: {}", contentType);
        return false;
    }
}