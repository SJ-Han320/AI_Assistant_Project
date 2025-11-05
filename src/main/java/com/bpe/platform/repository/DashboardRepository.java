package com.bpe.platform.repository;

import com.bpe.platform.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Integer> {
    
    /**
     * 특정 사용자의 대시보드를 order 순서대로 조회
     */
    @Query("SELECT d FROM Dashboard d WHERE d.dUser = :userId ORDER BY d.dOrder ASC")
    List<Dashboard> findByDUserOrderByDOrderAsc(@Param("userId") Long userId);
}

