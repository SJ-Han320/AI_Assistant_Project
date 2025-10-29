package com.bpe.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageUploadService {
    
    @Value("${app.upload.dir:target/classes/static/images/profiles/}")
    private String uploadDir;
    
    private static final String DEFAULT_IMAGE = "/images/default/default-avatar.svg";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");

    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        if (file.isEmpty()) {
            return DEFAULT_IMAGE;
        }

        // 파일 유효성 검사
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IOException("이미지 파일 (JPG, PNG, GIF, WebP)만 업로드 가능합니다.");
        }

        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = "profile_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(newFileName);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 웹 접근 가능한 경로 반환
        return "/images/profiles/" + newFileName;
    }

    public void deleteProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.equals(DEFAULT_IMAGE)) {
            // 웹 경로를 실제 파일 시스템 경로로 변환
            String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, fileName);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("프로필 이미지 삭제 실패: " + filePath + " - " + e.getMessage());
            }
        }
    }

    public String getDefaultImagePath() {
        return DEFAULT_IMAGE;
    }
}
