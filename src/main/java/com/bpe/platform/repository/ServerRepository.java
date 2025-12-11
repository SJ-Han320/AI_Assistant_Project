package com.bpe.platform.repository;

import com.bpe.platform.entity.Server;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ServerRepository {

    private final JdbcTemplate jdbcTemplate;

    // BPE_STAGE 데이터베이스를 사용하기 위해 primaryJdbcTemplate 명시적 사용
    public ServerRepository(@Qualifier("primaryJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 특정 Rack에 속한 서버 목록 조회
     * @param rmSeq Rack 번호
     * @return 서버 리스트
     */
    public List<Server> findByRmSeq(Integer rmSeq) {
        try {
            String sql = "SELECT sm_seq, rm_seq, sm_user, sm_name, sm_model, sm_cpu, sm_mem, sm_hdd, sm_order, sm_c_date, " +
                        "       sm_os, sm_main, sm_sub " +
                        "FROM server_mng " +
                        "WHERE rm_seq = ? " +
                        "ORDER BY sm_order ASC, sm_seq ASC";
            
            List<Server> servers = jdbcTemplate.query(sql, new ServerRowMapper(), rmSeq);
            
            if (servers == null || servers.isEmpty()) {
                return new ArrayList<>();
            }
            
            return servers;
        } catch (Exception e) {
            throw new RuntimeException("Server 테이블 조회 실패: " + e.getMessage() + 
                " (원인: " + e.getClass().getSimpleName() + ")", e);
        }
    }

    /**
     * Server RowMapper
     */
    private static class ServerRowMapper implements RowMapper<Server> {
        @Override
        public Server mapRow(ResultSet rs, int rowNum) throws SQLException {
            Server server = new Server();
            
            try {
                server.setSmSeq(rs.getInt("sm_seq"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setRmSeq(rs.getObject("rm_seq") != null ? rs.getInt("rm_seq") : null);
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmUser(rs.getObject("sm_user") != null ? rs.getLong("sm_user") : null);
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmName(rs.getString("sm_name"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmModel(rs.getString("sm_model"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmCpu(rs.getString("sm_cpu"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmMem(rs.getString("sm_mem"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmHdd(rs.getString("sm_hdd"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmOrder(rs.getObject("sm_order") != null ? rs.getInt("sm_order") : null);
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                java.sql.Timestamp smCDate = rs.getTimestamp("sm_c_date");
                if (smCDate != null) {
                    server.setSmCDate(smCDate.toLocalDateTime());
                }
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmOs(rs.getString("sm_os"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmMain(rs.getString("sm_main"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                server.setSmSub(rs.getString("sm_sub"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            return server;
        }
    }
}

