package com.bpe.platform.controller;

import com.bpe.platform.entity.Server;
import com.bpe.platform.entity.ServerIssue;
import com.bpe.platform.service.ServerService;
import com.bpe.platform.service.ServerIssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 서버 현황 관리 Server 컨트롤러
 */
@RestController
@RequestMapping("/api/server-status")
public class ServerController {

    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    private ServerService serverService;

    @Autowired
    private ServerIssueService serverIssueService;

    @GetMapping("/racks/{rmSeq}/servers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getServersByRmSeq(@PathVariable Integer rmSeq) {
        logger.info("Rack {}의 서버 목록 조회 요청", rmSeq);
        
        try {
            List<Server> servers = serverService.getServersByRmSeq(rmSeq);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("servers", servers);
            response.put("count", servers.size());
            response.put("rmSeq", rmSeq);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("서버 목록 조회 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "데이터를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/servers/{smSeq}/issues")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getServerIssues(@PathVariable Integer smSeq) {
        logger.info("서버 {}의 이슈 목록 조회 요청", smSeq);
        
        try {
            List<ServerIssue> issues = serverIssueService.getIssuesBySmSeq(smSeq);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("issues", issues);
            response.put("count", issues.size());
            response.put("smSeq", smSeq);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("서버 이슈 목록 조회 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "서버 이슈를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

