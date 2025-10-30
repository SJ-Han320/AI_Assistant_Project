package com.bpe.platform.controller;

import com.bpe.platform.entity.SparkTask;
import com.bpe.platform.service.SparkTaskService;
import com.bpe.platform.service.UserService;
import com.bpe.platform.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/data")
public class DataController {

    @Autowired
    private SparkTaskService sparkTaskService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CodeService codeService;

    @GetMapping("/supply")
    public String dataSupply(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("stStrDate").descending());
            Page<SparkTask> taskPage;
            
            if (status != null && !status.isEmpty()) {
                taskPage = sparkTaskService.getTasksByStatus(status, pageable);
            } else {
                taskPage = sparkTaskService.getAllTasks(pageable);
            }
            
            // 상태별 통계 계산
            Map<String, Long> statusCounts = sparkTaskService.getStatusCounts();
            
            model.addAttribute("tasks", taskPage.getContent());
            model.addAttribute("currentPage", taskPage.getNumber());
            model.addAttribute("totalPages", taskPage.getTotalPages());
            model.addAttribute("totalElements", taskPage.getTotalElements());
            model.addAttribute("size", taskPage.getSize());
            model.addAttribute("hasNext", taskPage.hasNext());
            model.addAttribute("hasPrevious", taskPage.hasPrevious());
            model.addAttribute("isFirst", taskPage.isFirst());
            model.addAttribute("isLast", taskPage.isLast());
            model.addAttribute("statusCounts", statusCounts);
            model.addAttribute("currentStatus", status);
            
            return "data-supply";
        } catch (Exception e) {
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "data-supply";
        }
    }

    @GetMapping("/create")
    public String projectCreate(Model model) {
        try {
            // 요청 필드 목록을 code 테이블에서 가져오기
            var fieldList = codeService.getFieldList();
            model.addAttribute("fieldList", fieldList);
            return "project-create";
        } catch (Exception e) {
            model.addAttribute("error", "필드 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "project-create";
        }
    }

    @PostMapping("/create")
    public String createProject(
            @RequestParam String projectName,
            @RequestParam String dataQuery,
            @RequestParam(required = false) String[] selectedFields,
            @RequestParam(required = false) String storageType,
            @RequestParam(required = false) String dbHost,
            @RequestParam(required = false) String dbDatabase,
            @RequestParam(required = false) String dbTable,
            @RequestParam(required = false) String dbUser,
            @RequestParam(required = false) String dbPassword,
            Model model) {
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            var currentUser = userService.findByUsername(username).orElse(null);
            
            if (currentUser == null) {
                model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
                return "project-create";
            }
            
            // 선택된 필드들을 콤마로 구분된 문자열로 변환
            String selectedFieldsStr = "";
            if (selectedFields != null && selectedFields.length > 0) {
                selectedFieldsStr = String.join(",", selectedFields);
            }
            
            // SparkTask 생성
            SparkTask task = new SparkTask();
            task.setStName(projectName);
            task.setStQuery(dataQuery);
            task.setStHost(dbHost);
            task.setStDb(dbDatabase);
            task.setStTable(dbTable);
            task.setStDbId(dbUser);
            task.setStDbPw(dbPassword);
            task.setStField(selectedFieldsStr);
            task.setStProgress(0);
            task.setStStatus("W"); // 대기 상태
            task.setStUser(currentUser.getId());
            task.setStStrDate(LocalDateTime.now()); // 현재 시간 설정
            
            sparkTaskService.saveTask(task);
            
            model.addAttribute("success", "프로젝트가 성공적으로 생성되었습니다.");
            
            // 필드 목록 다시 로드
            var fieldList = codeService.getFieldList();
            model.addAttribute("fieldList", fieldList);
            
            return "project-create";
        } catch (Exception e) {
            model.addAttribute("error", "프로젝트 생성 중 오류가 발생했습니다: " + e.getMessage());
            
            // 필드 목록 다시 로드
            try {
                var fieldList = codeService.getFieldList();
                model.addAttribute("fieldList", fieldList);
            } catch (Exception ex) {
                // 필드 목록 로드 실패 시 무시
            }
            
            return "project-create";
        }
    }

    // JSON API 엔드포인트들
    @GetMapping("/api/supply")
    public ResponseEntity<Map<String, Object>> getDataSupplyApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("stStrDate").descending());
            Page<SparkTask> taskPage;
            
            if (status != null && !status.isEmpty()) {
                taskPage = sparkTaskService.getTasksByStatus(status, pageable);
            } else {
                taskPage = sparkTaskService.getAllTasks(pageable);
            }
            
            // 상태별 통계 계산
            Map<String, Long> statusCounts = sparkTaskService.getStatusCounts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("tasks", taskPage.getContent());
            response.put("currentPage", taskPage.getNumber());
            response.put("totalPages", taskPage.getTotalPages());
            response.put("totalElements", taskPage.getTotalElements());
            response.put("size", taskPage.getSize());
            response.put("hasNext", taskPage.hasNext());
            response.put("hasPrevious", taskPage.hasPrevious());
            response.put("isFirst", taskPage.isFirst());
            response.put("isLast", taskPage.isLast());
            response.put("statusCounts", statusCounts);
            response.put("currentStatus", status);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/api/fields")
    public ResponseEntity<Map<String, Object>> getFieldsApi() {
        try {
            var fieldList = codeService.getFieldList();
            Map<String, Object> response = new HashMap<>();
            response.put("fields", fieldList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "필드 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
