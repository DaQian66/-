package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("文件不能为空");

        String originalName = file.getOriginalFilename();
        String suffix = originalName != null && originalName.contains(".") ?
                originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
        String newName = UUID.randomUUID().toString() + suffix;

        // 使用绝对路径避免相对路径找不到的问题
        String absoluteUploadPath = new java.io.File(uploadPath).getAbsoluteFile().getPath();
        String dateDir = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dir = absoluteUploadPath + "/images/" + dateDir;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            boolean created = dirFile.mkdirs();
            if (!created) {
                throw new IllegalStateException("无法创建上传目录: " + dir);
            }
        }

        try {
            file.transferTo(new File(dirFile, newName));
        } catch (IOException e) {
            throw new IllegalArgumentException("文件上传失败: " + e.getMessage());
        }

        Map<String, String> result = new HashMap<>();
        String path = "/uploads/images/" + dateDir + "/" + newName;
        result.put("url", path);
        result.put("name", originalName);
        return ApiResponse.success("上传成功", result);
    }

    @PostMapping("/face")
    public ApiResponse<Map<String, String>> uploadFace(@RequestParam("file") MultipartFile file) {
        return uploadImage(file);
    }

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return uploadImage(file);
    }
}