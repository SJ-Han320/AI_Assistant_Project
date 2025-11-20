package com.bpe.platform.controller;

import com.bpe.platform.entity.ApiSupplyCompanyDetail;
import com.bpe.platform.service.ApiSupplyCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
}

