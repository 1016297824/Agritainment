package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.Result;
import com.agritainment.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @PostMapping
    @RequireRole({RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return Result.error(40001, "文件不能为空");

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Paths.get(uploadPath);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());

        return Result.ok(Map.of("url", "/uploads/" + filename, "filename", filename));
    }
}
