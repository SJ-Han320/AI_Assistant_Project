package com.bpe.platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSetup implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            // users 테이블이 이미 존재하는지 확인
            String checkTable = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'BPE_STAGE' AND table_name = 'users'";
            Integer tableCount = jdbcTemplate.queryForObject(checkTable, Integer.class);
            
            if (tableCount == 0) {
                // users 테이블 생성
                String createTable = """
                    CREATE TABLE users (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(255) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255),
                        role VARCHAR(255) DEFAULT 'USER',
                        enabled BIT DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """;
                jdbcTemplate.execute(createTable);
                System.out.println("✅ users 테이블이 생성되었습니다.");
            }
            
            // ADMIN 계정이 이미 존재하는지 확인
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
            Integer adminCount = jdbcTemplate.queryForObject(checkAdmin, Integer.class);
            
            if (adminCount == 0) {
                // ADMIN 계정 생성
                String encodedPassword = passwordEncoder.encode("123");
                String insertAdmin = """
                    INSERT INTO users (username, password, name, email, role, enabled) 
                    VALUES ('admin', ?, '관리자', 'admin@bpe.com', 'ADMIN', 1)
                    """;
                jdbcTemplate.update(insertAdmin, encodedPassword);
                System.out.println("✅ ADMIN 계정이 생성되었습니다.");
                System.out.println("   사용자명: admin");
                System.out.println("   비밀번호: 123");
            } else {
                // 기존 admin 계정의 비밀번호를 123으로 업데이트
                String encodedPassword = passwordEncoder.encode("123");
                String updatePassword = "UPDATE users SET password = ? WHERE username = 'admin'";
                jdbcTemplate.update(updatePassword, encodedPassword);
                System.out.println("ℹ️  ADMIN 계정 비밀번호가 '123'으로 업데이트되었습니다.");
            }
            
            // 추가 사용자들 생성 (비활성화 - 수동으로 데이터 관리)
            // createAdditionalUsers();
            
        } catch (Exception e) {
            System.err.println("❌ 데이터베이스 설정 중 오류 발생: " + e.getMessage());
        }
    }
    
    private void createAdditionalUsers() {
        // 추가 사용자 정보
        String[][] users = {
            {"developer", "dev123", "개발자", "dev@bpe.com", "USER"},
            {"manager", "mgr123", "매니저", "manager@bpe.com", "USER"},
            {"tester", "test123", "테스터", "tester@bpe.com", "USER"}
        };
        
        for (String[] userInfo : users) {
            String username = userInfo[0];
            String password = userInfo[1];
            String name = userInfo[2];
            String email = userInfo[3];
            String role = userInfo[4];
            
            // 사용자가 이미 존재하는지 확인
            String checkUser = "SELECT COUNT(*) FROM users WHERE username = ?";
            Integer userCount = jdbcTemplate.queryForObject(checkUser, Integer.class, username);
            
            if (userCount == 0) {
                // 새 사용자 생성
                String encodedPassword = passwordEncoder.encode(password);
                String insertUser = """
                    INSERT INTO users (username, password, name, email, role, enabled) 
                    VALUES (?, ?, ?, ?, ?, 1)
                    """;
                jdbcTemplate.update(insertUser, username, encodedPassword, name, email, role);
                System.out.println("✅ " + name + " 계정이 생성되었습니다.");
                System.out.println("   사용자명: " + username);
                System.out.println("   비밀번호: " + password);
            } else {
                System.out.println("ℹ️  " + name + " 계정이 이미 존재합니다.");
            }
        }
    }
}

