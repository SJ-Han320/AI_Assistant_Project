package com.bpe.platform.controller;

import com.bpe.platform.entity.User;
import com.bpe.platform.service.UserService;
import com.bpe.platform.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/members")
public class MemberController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ImageUploadService imageUploadService;

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
            Page<User> userPage = userService.findAllUsers(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", userPage.getContent());
            response.put("currentPage", userPage.getNumber());
            response.put("totalPages", userPage.getTotalPages());
            response.put("totalElements", userPage.getTotalElements());
            response.put("size", userPage.getSize());
            response.put("hasNext", userPage.hasNext());
            response.put("hasPrevious", userPage.hasPrevious());
            response.put("isFirst", userPage.isFirst());
            response.put("isLast", userPage.isLast());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "멤버 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/upload-profile")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일이 선택되지 않았습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 현재 로그인한 사용자 정보 가져오기
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 기존 프로필 이미지 삭제
            if (user.getProfileImage() != null) {
                imageUploadService.deleteProfileImage(user.getProfileImage());
            }
            
            // 새 프로필 이미지 업로드
            String imagePath = imageUploadService.uploadProfileImage(file, user.getId());
            user.setProfileImage(imagePath);
            userService.save(user);
            
            response.put("success", true);
            response.put("imagePath", imagePath);
            response.put("message", "프로필 이미지가 성공적으로 업로드되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/delete-profile")
    public ResponseEntity<Map<String, Object>> deleteProfileImage(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (user.getProfileImage() != null) {
                imageUploadService.deleteProfileImage(user.getProfileImage());
                user.setProfileImage(null);
                userService.save(user);
            }
            
            response.put("success", true);
            response.put("message", "프로필 이미지가 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
