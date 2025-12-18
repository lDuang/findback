package com.lostfound.backend.service;

import com.lostfound.backend.entity.MediaFile;
import com.lostfound.backend.repository.MediaFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private final MediaFileRepository mediaFileRepository;

    public FileStorageService(MediaFileRepository mediaFileRepository) {
        this.mediaFileRepository = mediaFileRepository;
    }

    public MediaFile store(MultipartFile file, Long uploaderId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        Path destinationDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(destinationDir);

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path destination = destinationDir.resolve(storedName);
        file.transferTo(destination);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(storedName)
                .toUriString();

        MediaFile mediaFile = new MediaFile();
        mediaFile.setFilename(originalName.isEmpty() ? storedName : originalName);
        mediaFile.setUrl(url);
        mediaFile.setUploaderId(uploaderId);
        return mediaFileRepository.save(mediaFile);
    }
}
