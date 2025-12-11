package com.bpe.platform.repository;

import com.bpe.platform.entity.ServerIssue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ServerIssueRepository {

    private final JdbcTemplate jdbcTemplate;

    // BPE_STAGE 데이터베이스를 사용하기 위해 primaryJdbcTemplate 명시적 사용
    public ServerIssueRepository(@Qualifier("primaryJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 특정 서버의 이슈 목록 조회 (si_date 역순)
     * @param smSeq 서버 번호
     * @return 서버 이슈 리스트
     */
    public List<ServerIssue> findBySmSeq(Integer smSeq) {
        try {
            String sql = "SELECT si_seq, sm_seq, si_issue, si_user, si_date " +
                        "FROM server_issue " +
                        "WHERE sm_seq = ? " +
                        "ORDER BY si_date DESC";
            
            List<ServerIssue> issues = jdbcTemplate.query(sql, new ServerIssueRowMapper(), smSeq);
            
            if (issues == null || issues.isEmpty()) {
                return new ArrayList<>();
            }
            
            return issues;
        } catch (Exception e) {
            throw new RuntimeException("ServerIssue 테이블 조회 실패: " + e.getMessage() + 
                " (원인: " + e.getClass().getSimpleName() + ")", e);
        }
    }

    /**
     * ServerIssue RowMapper
     */
    private static class ServerIssueRowMapper implements RowMapper<ServerIssue> {
        @Override
        public ServerIssue mapRow(ResultSet rs, int rowNum) throws SQLException {
            ServerIssue issue = new ServerIssue();
            
            try {
                issue.setSiSeq(rs.getInt("si_seq"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                issue.setSmSeq(rs.getObject("sm_seq") != null ? rs.getInt("sm_seq") : null);
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                issue.setSiIssue(rs.getString("si_issue"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                issue.setSiUser(rs.getObject("si_user") != null ? rs.getLong("si_user") : null);
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                java.sql.Timestamp siDate = rs.getTimestamp("si_date");
                if (siDate != null) {
                    issue.setSiDate(siDate.toLocalDateTime());
                }
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            return issue;
        }
    }
}

