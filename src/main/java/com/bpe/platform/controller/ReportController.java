package com.bpe.platform.controller;

import com.bpe.platform.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/generate")
    @ResponseBody
    public Map<String, Object> generateReport() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Python 스크립트 실행
            String result = reportService.executePythonScript();
            
            if (result != null && result.contains("성공")) {
                response.put("success", true);
                response.put("message", "월간 보고서가 성공적으로 생성되었습니다.");
                response.put("downloadUrl", "/report/download");
            } else {
                response.put("success", false);
                response.put("message", "보고서 생성 중 오류가 발생했습니다: " + result);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "보고서 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadReport() {
        try {
            File reportFile = reportService.getLatestReportFile();
            
            if (reportFile != null && reportFile.exists()) {
                Resource resource = new FileSystemResource(reportFile);
                
                // 파일명을 UTF-8로 인코딩하여 한글 파일명 문제 해결
                String encodedFileName = URLEncoder.encode(reportFile.getName(), StandardCharsets.UTF_8.toString());
                
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename*=UTF-8''" + encodedFileName)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
