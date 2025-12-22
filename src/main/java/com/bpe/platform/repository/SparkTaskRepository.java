package com.bpe.platform.repository;

import com.bpe.platform.entity.SparkTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SparkTaskRepository extends JpaRepository<SparkTask, Integer> {
    
    // 페이징된 작업 조회 (User와 JOIN)
    @Query("SELECT s FROM SparkTask s LEFT JOIN FETCH s.user ORDER BY s.stStrDate DESC")
    Page<SparkTask> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 최근 10개 작업 조회 (페이징 없이, User와 JOIN)
    @Query("SELECT s FROM SparkTask s LEFT JOIN FETCH s.user ORDER BY s.stStrDate DESC")
    List<SparkTask> findTop10ByOrderByCreatedAtDesc();
    
    // 상태별 작업 조회
    List<SparkTask> findByStStatus(String stStatus);
    
    // 상태별 페이징된 작업 조회 (User와 JOIN)
    @Query("SELECT s FROM SparkTask s LEFT JOIN FETCH s.user WHERE s.stStatus = :status ORDER BY s.stStrDate DESC")
    Page<SparkTask> findByStStatusOrderByCreatedAtDesc(@Param("status") String status, Pageable pageable);
    
    // 사용자별 작업 조회
    List<SparkTask> findByStUser(Long stUser);
    
    // 상태별 개수 조회
    long countByStStatus(String stStatus);
    
    // 페이징된 모든 작업 조회 (stStrDate 기준)
    @Query("SELECT s FROM SparkTask s LEFT JOIN FETCH s.user ORDER BY s.stStrDate DESC")
    Page<SparkTask> findAllByOrderByStStrDateDesc(Pageable pageable);
    
    // 상태별 페이징된 작업 조회 (stStrDate 기준)
    @Query("SELECT s FROM SparkTask s LEFT JOIN FETCH s.user WHERE s.stStatus = :status ORDER BY s.stStrDate DESC")
    Page<SparkTask> findByStStatusOrderByStStrDateDesc(@Param("status") String status, Pageable pageable);
    
    // 프로젝트명으로 최근 생성된 작업 조회 (중복 체크용)
    @Query("SELECT s FROM SparkTask s WHERE s.stName = :stName ORDER BY s.stStrDate DESC")
    List<SparkTask> findByStNameOrderByStStrDateDesc(@Param("stName") String stName);
}