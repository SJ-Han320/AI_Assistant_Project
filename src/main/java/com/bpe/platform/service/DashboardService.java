package com.bpe.platform.service;

import com.bpe.platform.entity.Dashboard;
import com.bpe.platform.repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {
    
    @Autowired
    private DashboardRepository dashboardRepository;
    
    /**
     * 특정 사용자의 대시보드 목록 조회 (order 순서대로)
     */
    public List<Dashboard> getDashboardsByUser(Long userId) {
        return dashboardRepository.findByDUserOrderByDOrderAsc(userId);
    }
    
    /**
     * 대시보드 저장
     */
    public Dashboard saveDashboard(Dashboard dashboard) {
        return dashboardRepository.save(dashboard);
    }
    
    /**
     * 대시보드 삭제
     */
    public void deleteDashboard(Integer seq) {
        dashboardRepository.deleteById(seq);
    }
}

