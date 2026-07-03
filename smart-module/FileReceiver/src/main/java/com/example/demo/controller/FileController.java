package com.example.demo.controller;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @ClassName: FileController
 * @Package com.example.demo.controller
 * @Description:
 * @Author wuxinjian
 * @Date 2023/12/18 10:17
 * @Version V1.0
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file-receiver.upload-root:${java.io.tmpdir}/file-receiver}")
    private String uploadRoot;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, @RequestParam("filePath") String filePath) throws IOException {
        // 【已废弃】/file/upload 推送接口将在下个版本移除，请迁移到拉取模式（file-receiver.pull.*）
        log.warn("【已废弃】/file/upload 推送接口将在下个版本移除，请迁移到拉取模式");
        Path target = resolveTargetPath(filePath);
        log.info("保存文件: {}", target);
        Files.createDirectories(target.getParent());
        FileUtil.writeBytes(file.getBytes(), target.toString());
        return "ok";
    }

    private Path resolveTargetPath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filePath is required");
        }

        Path requestedPath = Paths.get(filePath);
        if (requestedPath.isAbsolute()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "absolute filePath is not allowed");
        }

        Path root = Paths.get(uploadRoot).toAbsolutePath().normalize();
        Path target = root.resolve(requestedPath).normalize();
        if (!target.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filePath is outside upload root");
        }
        return target;
    }
}
