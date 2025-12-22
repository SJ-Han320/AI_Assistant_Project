package com.bpe.platform.config;

// 이 클래스는 완전히 비활성화됨 - st_status='S'로 자동 생성되는 문제의 원인일 수 있음
// 더 이상 사용하지 않으므로 전체 클래스를 주석 처리

/*
import com.bpe.platform.entity.SparkTask;
import com.bpe.platform.repository.SparkTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// @Component  // 비활성화 - 더 이상 자동으로 샘플 데이터를 생성하지 않음
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private SparkTaskRepository sparkTaskRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 항상 테스트 데이터 생성 (기존 데이터 삭제 후)
        createSampleData();
    }
    
    private void createSampleData() {
        // 기존 데이터가 있으면 삭제
        sparkTaskRepository.deleteAll();
        
        String[] statuses = {"S", "C", "W", "E"};
        String[] projects = {"Quetta Data 추출", "시스템 모니터링", "로그 분석", "성능 최적화", "보안 점검", "데이터 백업", "클러스터 상태 확인", "메모리 최적화", "네트워크 진단", "알림 설정"};
        int[] progress = {75, 100, 25, 0, 60, 90, 45, 30, 80, 15};
        Long[] users = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}; // 사용자 ID
        String[] queries = {"SELECT * FROM logs WHERE date >= '2024-01-01'", "SELECT COUNT(*) FROM events", "SELECT * FROM metrics WHERE cpu > 80", "SELECT * FROM errors WHERE level = 'ERROR'", "SELECT * FROM users WHERE status = 'active'"};
        
        for (int i = 0; i < 120; i++) {  // 120개 데이터 생성
            SparkTask task = new SparkTask(
                projects[i % projects.length],  // 프로젝트 순환
                users[i % users.length],        // 사용자 ID 순환
                statuses[i % statuses.length],  // 상태 순환
                queries[i % queries.length]     // 쿼리 순환
            );
            // 진행률 설정
            task.setStProgress(progress[i % progress.length]);
            sparkTaskRepository.save(task);
            System.out.println("생성된 작업: " + task.getStName() + " - " + task.getStStatus() + " - " + task.getStProgress() + "%");
        }
        
        System.out.println("테스트 데이터가 생성되었습니다. 총 " + sparkTaskRepository.count() + "개");
    }
}
*/
