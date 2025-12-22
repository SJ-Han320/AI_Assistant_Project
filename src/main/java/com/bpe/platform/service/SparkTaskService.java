package com.bpe.platform.service;

import com.bpe.platform.entity.SparkTask;
import com.bpe.platform.repository.SparkTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SparkTaskService {
    
    @Autowired
    private SparkTaskRepository sparkTaskRepository;
    
    // 페이징된 작업 조회
    public Page<SparkTask> getTasksWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sparkTaskRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    // 페이징된 작업 조회 (상태 필터링 포함)
    public Page<SparkTask> getTasksWithPagingAndStatus(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        if (status == null || status.isEmpty()) {
            return sparkTaskRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            // 한글 상태명을 코드로 변환
            String statusCode = convertStatusToCode(status);
            return sparkTaskRepository.findByStStatusOrderByCreatedAtDesc(statusCode, pageable);
        }
    }
    
    // 한글 상태명을 코드로 변환
    private String convertStatusToCode(String status) {
        switch (status) {
            case "진행중": return "S";
            case "완료": return "C";
            case "대기": return "W";
            case "오류": return "E";
            default: return status;
        }
    }
    
    // 모든 작업 조회 (최근 10개) - 페이징 없이
    public List<SparkTask> getAllTasks() {
        return sparkTaskRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    // 상태별 작업 조회
    public List<SparkTask> getTasksByStatus(String status) {
        return sparkTaskRepository.findByStStatus(status);
    }
    
    // 사용자별 작업 조회
    public List<SparkTask> getTasksByUser(Long stUser) {
        return sparkTaskRepository.findByStUser(stUser);
    }
    
    // 작업 저장
    public SparkTask saveTask(SparkTask task) {
        return sparkTaskRepository.save(task);
    }
    
    // 작업 삭제
    public void deleteTask(Integer id) {
        sparkTaskRepository.deleteById(id);
    }
    
    // 통계 정보 조회
    public long getTotalCount() {
        return sparkTaskRepository.count();
    }
    
    public long getCompletedCount() {
        return sparkTaskRepository.countByStStatus("C");
    }
    
    public long getInProgressCount() {
        return sparkTaskRepository.countByStStatus("S");
    }
    
    public long getWaitingCount() {
        return sparkTaskRepository.countByStStatus("W");
    }
    
    public long getErrorCount() {
        return sparkTaskRepository.countByStStatus("E");
    }
    
    // 페이징된 모든 작업 조회
    public Page<SparkTask> getAllTasks(Pageable pageable) {
        return sparkTaskRepository.findAllByOrderByStStrDateDesc(pageable);
    }
    
    // 상태별 페이징된 작업 조회
    public Page<SparkTask> getTasksByStatus(String status, Pageable pageable) {
        return sparkTaskRepository.findByStStatusOrderByStStrDateDesc(status, pageable);
    }
    
    // 상태별 통계 조회
    public Map<String, Long> getStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", getTotalCount());
        counts.put("C", getCompletedCount());
        counts.put("S", getInProgressCount());
        counts.put("W", getWaitingCount());
        counts.put("E", getErrorCount());
        return counts;
    }
    
    // 프로젝트명으로 작업 조회 (중복 체크용)
    public List<SparkTask> findByStName(String stName) {
        return sparkTaskRepository.findByStNameOrderByStStrDateDesc(stName);
    }
}
