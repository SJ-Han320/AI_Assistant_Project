package com.bpe.platform.controller;

import com.bpe.platform.entity.Rack;
import com.bpe.platform.repository.RackRepository;
import com.bpe.platform.service.RackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 서버 현황 관리 Rack 컨트롤러
 */
@RestController
@RequestMapping("/api/server-status")
public class RackController {

    private static final Logger logger = LoggerFactory.getLogger(RackController.class);

    @Autowired
    private RackService rackService;
    
    @Autowired
    private RackRepository rackRepository;

    @GetMapping("/racks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getRacks() {
        logger.info("Rack 목록 조회 요청");
        
        try {
            List<Rack> racks = rackService.getAllRacks();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("racks", racks);
            response.put("count", racks.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Rack 목록 조회 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "데이터를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/table-structure")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTableStructure() {
        logger.info("rack_mng 테이블 구조 조회 요청");
        
        try {
            List<Map<String, Object>> structure = rackRepository.getTableStructure();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tableName", "rack_mng");
            response.put("columns", structure);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("테이블 구조 조회 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "테이블 구조를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

