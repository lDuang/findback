package com.lostfound.backend.controller;

import com.lostfound.backend.entity.MediaFile;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.service.AuthService;
import com.lostfound.backend.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final AuthService authService;

    public FileController(FileStorageService fileStorageService, AuthService authService) {
        this.fileStorageService = fileStorageService;
        this.authService = authService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                      HttpServletRequest servletRequest) throws IOException {
        UserContext context = authService.extractContext(
                servletRequest.getHeader("X-User-Id"),
                servletRequest.getHeader("X-User-Role"));
        Long uploaderId = context.getUserId();

        MediaFile saved = fileStorageService.store(file, uploaderId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", saved.getId());
        payload.put("url", saved.getUrl());
        payload.put("filename", saved.getFilename());
        payload.put("uploaderId", saved.getUploaderId());
        return ResponseEntity.ok(payload);
    }
}
