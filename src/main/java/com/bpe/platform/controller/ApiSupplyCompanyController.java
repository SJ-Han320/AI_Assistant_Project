package com.bpe.platform.controller;

import com.bpe.platform.entity.ApiSupplyCompanyDetail;
import com.bpe.platform.service.ApiSupplyCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 데이터 공급 API 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/data-supply-api")
public class ApiSupplyCompanyController {

    private static final Logger logger = LoggerFactory.getLogger(ApiSupplyCompanyController.class);

    @Autowired
    private ApiSupplyCompanyService apiSupplyCompanyService;

    @GetMapping("/projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String filter) {
        
        logger.info("프로젝트 목록 조회 요청 - page: {}, size: {}, filter: {}", page, size, filter);
        
        try {
            Map<String, Object> result = apiSupplyCompanyService.getProjects(page, size, filter);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("프로젝트 목록 조회 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "데이터를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/projects/{comSeq}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiSupplyCompanyDetail> getProjectDetail(@PathVariable Integer comSeq) {
        logger.info("프로젝트 상세 정보 조회 요청 - comSeq: {}", comSeq);
        
        try {
            ApiSupplyCompanyDetail detail = apiSupplyCompanyService.getProjectDetail(comSeq);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            logger.error("프로젝트 상세 정보 조회 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody ApiSupplyCompanyDetail detail) {
        logger.info("프로젝트 추가 요청 - comName: {}", detail.getComName());
        
        try {
            Integer ascSeq = apiSupplyCompanyService.createProject(detail);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로젝트가 성공적으로 추가되었습니다.");
            response.put("ascSeq", ascSeq);
            response.put("comKey", detail.getComKey());
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("프로젝트 추가 검증 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            logger.error("프로젝트 추가 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "프로젝트 추가 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/projects/{comKey}/error")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUseYnToError(@PathVariable String comKey) {
        logger.info("프로젝트 오류 상태 업데이트 요청 - comKey: {}", comKey);
        
        try {
            apiSupplyCompanyService.updateUseYnToErrorByComKey(comKey);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로젝트 상태가 오류로 업데이트되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("프로젝트 상태 업데이트 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "프로젝트 상태 업데이트 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 외부 API 호출 프록시 (CORS 문제 해결)
     * @param apiType API 타입 (reloadCompany2, reloadCompany, reloadHost)
     * @return 외부 API 응답
     */
    @GetMapping("/proxy/{apiType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> callExternalApi(@PathVariable String apiType) {
        logger.info("외부 API 호출 프록시 - apiType: {}", apiType);
        
        try {
            String fixedComKey = "d6ded044e0184a94ba37af8de2223538";
            String apiUrl;
            
            switch (apiType) {
                case "reloadCompany2":
                    apiUrl = "https://data-api.quetta.co.kr/manage/reloadCompany/2?comKey=" + fixedComKey;
                    break;
                case "reloadCompany":
                    apiUrl = "https://data-api.quetta.co.kr/manage/reloadCompany?comKey=" + fixedComKey;
                    break;
                case "reloadHost":
                    apiUrl = "https://data-api.quetta.co.kr/manage/reloadHost?comKey=" + fixedComKey;
                    break;
                default:
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "알 수 없는 API 타입입니다: " + apiType);
                    return ResponseEntity.status(400).body(errorResponse);
            }
            
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            org.springframework.http.HttpEntity<?> requestEntity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl,
                org.springframework.http.HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response.getBody());
            result.put("statusCode", response.getStatusCode().value());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("외부 API 호출 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "API 호출 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

