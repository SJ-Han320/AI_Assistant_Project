package com.bpe.platform.controller;

import com.bpe.platform.entity.User;
import com.bpe.platform.service.UserService;
import com.bpe.platform.service.ImageUploadService;
import com.bpe.platform.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
public class MainController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ImageUploadService imageUploadService;
    
    @Autowired
    private DashboardService dashboardService;

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
                
                // 사용자의 대시보드 목록 조회 (d_user로 필터링, d_order ASC 정렬)
                var dashboards = dashboardService.getDashboardsByUser(user.getId());
                model.addAttribute("dashboards", dashboards);
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
    
    /**
     * 대시보드 추가 API
     */
    @PostMapping("/api/dashboard/add")
    public ResponseEntity<Map<String, Object>> addDashboard(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String icon,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) Integer order,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("error", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                response.put("success", false);
                response.put("error", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 대시보드 생성
            com.bpe.platform.entity.Dashboard dashboard = new com.bpe.platform.entity.Dashboard();
            dashboard.setDUser(user.getId());
            dashboard.setDName(name);
            dashboard.setDDescription(description);
            dashboard.setDIcon(icon);
            dashboard.setDUrl(url != null ? url : "");
            
            // order가 없으면 마지막 순서로 설정
            if (order == null) {
                var existingDashboards = dashboardService.getDashboardsByUser(user.getId());
                order = existingDashboards.size() > 0 
                    ? existingDashboards.get(existingDashboards.size() - 1).getDOrder() + 1 
                    : 1;
            }
            dashboard.setDOrder(order);
            
            // 한국 시간으로 생성일시 설정
            dashboard.setDDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            
            // 대시보드 저장
            dashboardService.saveDashboard(dashboard);
            
            response.put("success", true);
            response.put("message", "대시보드가 성공적으로 추가되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "대시보드 추가 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 대시보드 삭제 API
     */
    @DeleteMapping("/api/dashboard/delete/{seq}")
    public ResponseEntity<Map<String, Object>> deleteDashboard(
            @PathVariable Integer seq,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("error", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                response.put("success", false);
                response.put("error", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 대시보드 조회
            var dashboard = dashboardService.getDashboardsByUser(user.getId())
                .stream()
                .filter(d -> d.getDSeq().equals(seq))
                .findFirst()
                .orElse(null);
            
            if (dashboard == null) {
                response.put("success", false);
                response.put("error", "대시보드를 찾을 수 없거나 삭제할 권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // 대시보드 삭제
            dashboardService.deleteDashboard(seq);
            
            response.put("success", true);
            response.put("message", "대시보드가 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "대시보드 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 대시보드 순서 업데이트 API
     */
    @PostMapping("/api/dashboard/update-order")
    public ResponseEntity<Map<String, Object>> updateDashboardOrder(
            @RequestBody List<Map<String, Object>> orderList,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("error", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                response.put("success", false);
                response.put("error", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 순서 업데이트
            for (Map<String, Object> item : orderList) {
                Integer seq = Integer.valueOf(item.get("seq").toString());
                Integer order = Integer.valueOf(item.get("order").toString());
                
                // 대시보드 조회
                var dashboard = dashboardService.getDashboardsByUser(user.getId())
                    .stream()
                    .filter(d -> d.getDSeq().equals(seq))
                    .findFirst()
                    .orElse(null);
                
                if (dashboard != null) {
                    dashboard.setDOrder(order);
                    dashboardService.saveDashboard(dashboard);
                }
            }
            
            response.put("success", true);
            response.put("message", "대시보드 순서가 업데이트되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "대시보드 순서 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}