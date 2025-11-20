package com.bpe.platform.service;

import com.bpe.platform.entity.ApiSupplyCompany;
import com.bpe.platform.entity.ApiSupplyCompanyDetail;
import com.bpe.platform.repository.ApiSupplyCompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiSupplyCompanyService {

    private static final Logger logger = LoggerFactory.getLogger(ApiSupplyCompanyService.class);

    @Autowired
    private ApiSupplyCompanyRepository repository;

    /**
     * 상태 계산
     * - use_yn이 'N'이면 '종료'
     * - use_yn이 'Y'이고 종료날짜까지 7일 넘게 남았으면 '운영'
     * - use_yn이 'Y'이고 종료날짜까지 7일 안 남았으면 '종료 임박'
     */
    private String calculateStatus(ApiSupplyCompany company) {
        if (company.getUseYn() == null || "N".equalsIgnoreCase(company.getUseYn())) {
            return "closed";
        }
        
        if (company.getEndDate() == null) {
            return "active"; // 종료날짜가 없으면 운영으로 처리
        }
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = company.getEndDate();
        
        if (endDate.isBefore(today)) {
            return "closed"; // 이미 종료일이 지났으면 종료
        }
        
        long daysUntilEnd = ChronoUnit.DAYS.between(today, endDate);
        
        if (daysUntilEnd <= 7) {
            return "expiring"; // 7일 이하면 종료 임박
        } else {
            return "active"; // 7일 넘게 남았으면 운영
        }
    }

    /**
     * 프로젝트 리스트 조회 (페이징)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param filter 필터 ('closed', 'expiring', 'active', '' 전체)
     * @return 프로젝트 리스트와 통계 정보
     */
    public Map<String, Object> getProjects(int page, int size, String filter) {
        logger.info("프로젝트 조회 - page: {}, size: {}, filter: {}", page, size, filter);
        
        try {
            logger.info("데이터 공급 API 프로젝트 조회 시작");
            // 전체 데이터 조회 (필터링은 메모리에서 처리)
            List<ApiSupplyCompany> allProjects = repository.findAll();
            if (allProjects == null) {
                allProjects = new ArrayList<>();
            }
            logger.info("전체 프로젝트 조회 완료: {}개", allProjects.size());
        
            // 상태 계산 및 필터링
            List<ApiSupplyCompany> filteredProjects = allProjects.stream()
            .map(project -> {
                String status = calculateStatus(project);
                project.setStatus(status);
                return project;
            })
            .filter(project -> {
                if (filter == null || filter.isEmpty()) {
                    return true;
                }
                return filter.equals(project.getStatus());
            })
            .collect(Collectors.toList());
        
        // 통계 계산
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("total", (long) allProjects.size());
        statusCounts.put("closed", allProjects.stream()
            .map(this::calculateStatus)
            .filter(s -> "closed".equals(s))
            .count());
        statusCounts.put("expiring", allProjects.stream()
            .map(this::calculateStatus)
            .filter(s -> "expiring".equals(s))
            .count());
        statusCounts.put("active", allProjects.stream()
            .map(this::calculateStatus)
            .filter(s -> "active".equals(s))
            .count());
        
        // 페이징 처리
        int totalItems = filteredProjects.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = page * size;
        int end = Math.min(start + size, totalItems);
        
        List<ApiSupplyCompany> pagedProjects = filteredProjects.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("projects", pagedProjects);
        result.put("statusCounts", statusCounts);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalItems", totalItems);
        result.put("hasPrevious", page > 0);
        result.put("hasNext", page < totalPages - 1);
        
        return result;
        } catch (Exception e) {
            logger.error("프로젝트 조회 중 오류 발생", e);
            throw new RuntimeException("프로젝트 데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 프로젝트 상세 정보 조회
     * @param comSeq 프로젝트 시퀀스
     * @return 프로젝트 상세 정보
     */
    public ApiSupplyCompanyDetail getProjectDetail(Integer comSeq) {
        logger.info("프로젝트 상세 정보 조회 - comSeq: {}", comSeq);
        return repository.findDetailByComSeq(comSeq);
    }
}

