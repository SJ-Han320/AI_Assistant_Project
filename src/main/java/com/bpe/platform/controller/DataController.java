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
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.core.ParameterizedTypeReference;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    
    private final RestTemplate restTemplate = new RestTemplate();

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
    public ResponseEntity<Map<String, Object>> createProject(
            @RequestParam String projectName,
            @RequestParam String dataQuery,
            @RequestParam(required = false) String[] selectedFields,
            @RequestParam(required = false) String storageType,
            @RequestParam(required = false) String dbHost,
            @RequestParam(required = false) String dbDatabase,
            @RequestParam(required = false) String dbDestination,
            @RequestParam(required = false) String dbUser,
            @RequestParam(required = false) String dbPassword) {
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            var currentUser = userService.findByUsername(username).orElse(null);
            
            if (currentUser == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 선택된 필드들을 콤마로 구분된 문자열로 변환
            String selectedFieldsStr = "";
            if (selectedFields != null && selectedFields.length > 0) {
                selectedFieldsStr = String.join(",", selectedFields);
            }
            
            // 저장소 타입 검증
            if (storageType == null || storageType.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "저장소 타입이 지정되지 않았습니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 중복 생성 방지: 최근 10초 내에 같은 프로젝트명으로 생성된 작업이 있는지 확인 (모든 상태 체크)
            LocalDateTime tenSecondsAgo = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(10);
            var recentTasks = sparkTaskService.findByStName(projectName);
            boolean hasRecentDuplicate = recentTasks.stream()
                .anyMatch(t -> t.getStStrDate() != null && 
                             t.getStStrDate().isAfter(tenSecondsAgo));
            
            if (hasRecentDuplicate) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "같은 프로젝트명으로 최근에 생성된 작업이 있습니다. 잠시 후 다시 시도해주세요.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // SparkTask 생성
            SparkTask task = new SparkTask();
            task.setStName(projectName);
            task.setStQuery(dataQuery);
            task.setStHost(dbHost);
            task.setStDb(dbDatabase);
            task.setStDestination(dbDestination);
            task.setStDbId(dbUser);
            task.setStDbPw(dbPassword);
            task.setStField(selectedFieldsStr);
            task.setStType(storageType != null ? storageType.trim() : null); // 저장소 타입 추가 (공백 제거)
            task.setStProgress(0);
            task.setStStatus("W"); // 대기 상태
            task.setStUser(currentUser.getId()); // 사용자 ID 설정
            task.setStStrDate(LocalDateTime.now(ZoneId.of("Asia/Seoul"))); // 한국 시간으로 현재 시간 설정
            
            SparkTask savedTask = sparkTaskService.saveTask(task);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로젝트가 성공적으로 생성되었습니다.");
            response.put("taskId", savedTask.getStSeq());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("프로젝트 생성 오류: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "프로젝트 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<Map<String, Object>> deleteProject(@PathVariable Integer taskId) {
        try {
            sparkTaskService.deleteTask(taskId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로젝트가 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "프로젝트 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
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
    
    /**
     * 외부 API 호출 프록시 엔드포인트 (CORS 문제 해결)
     * 프로젝트 등록 API 호출
     */
    @PostMapping("/api/register-project")
    public ResponseEntity<Map<String, Object>> registerProject(@RequestBody Map<String, Object> requestData) {
        try {
            String apiUrl = "https://de61dbe91e31.ngrok-free.app/register";
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("ngrok-skip-browser-warning", "true"); // ngrok 브라우저 경고 스킵
            
            // 요청 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
            
            // 외부 API 호출
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            // 응답 데이터 반환
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response.getBody());
            result.put("statusCode", response.getStatusCode().value());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "API 호출 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
