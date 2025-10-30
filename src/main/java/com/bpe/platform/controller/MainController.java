package com.bpe.platform.controller;

import com.bpe.platform.entity.User;
import com.bpe.platform.service.UserService;
import com.bpe.platform.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ImageUploadService imageUploadService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/main")
    public String main(Authentication authentication, Model model) {
        model.addAttribute("title", "BPE Platform - Main Dashboard");
        
        // 로그인한 사용자 정보 가져오기
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            if (user != null) {
                model.addAttribute("userName", user.getName());
                model.addAttribute("userUsername", user.getUsername());
                model.addAttribute("userEmail", user.getEmail());
                model.addAttribute("userRole", user.getRole());
                model.addAttribute("userProfileImage", user.getProfileImage() != null ? 
                    user.getProfileImage() : imageUploadService.getDefaultImagePath());
            } else {
                model.addAttribute("userName", username);
                model.addAttribute("userUsername", username);
                model.addAttribute("userProfileImage", imageUploadService.getDefaultImagePath());
            }
        }
        
        return "main";
    }

    @PostMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }
}