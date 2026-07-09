package com.attendance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication
public class AttendanceApplication {

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Value("${app.upload.face-path:./uploads/faces}")
    private String facePath;

    @Value("${app.upload.avatar-path:./uploads/avatars}")
    private String avatarPath;

    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);
        System.out.println("========================================");
        System.out.println("  员工考勤管理系统启动成功！");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("========================================");
    }

    /** 启动时自动创建上传目录 */
    @Bean
    public CommandLineRunner initUploadDirs() {
        return args -> {
            createDirIfNotExists(uploadPath);
            createDirIfNotExists(facePath);
            createDirIfNotExists(avatarPath);
            createDirIfNotExists(uploadPath + "/images");
            System.out.println("  上传目录已就绪: " + new File(uploadPath).getAbsoluteFile().getPath());
        };
    }

    private void createDirIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("  [OK] 创建目录: " + dir.getAbsolutePath());
            } else {
                System.err.println("  [FAIL] 无法创建目录: " + dir.getAbsolutePath());
            }
        }
    }
}