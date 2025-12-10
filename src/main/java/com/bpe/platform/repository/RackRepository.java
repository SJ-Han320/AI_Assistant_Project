package com.bpe.platform.repository;

import com.bpe.platform.entity.Rack;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RackRepository {

    private final JdbcTemplate jdbcTemplate;

    // BPE_STAGE 데이터베이스를 사용하기 위해 primaryJdbcTemplate 명시적 사용
    public RackRepository(@Qualifier("primaryJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * rack_mng 테이블 구조 확인
     * @return 테이블 컬럼 정보
     */
    public List<Map<String, Object>> getTableStructure() {
        try {
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT " +
                        "FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'rack_mng' " +
                        "ORDER BY ORDINAL_POSITION";
            
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> column = new HashMap<>();
                column.put("columnName", rs.getString("COLUMN_NAME"));
                column.put("dataType", rs.getString("DATA_TYPE"));
                column.put("isNullable", rs.getString("IS_NULLABLE"));
                column.put("columnDefault", rs.getString("COLUMN_DEFAULT"));
                column.put("columnComment", rs.getString("COLUMN_COMMENT"));
                return column;
            });
        } catch (Exception e) {
            throw new RuntimeException("rack_mng 테이블 구조 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 전체 Rack 목록 조회
     * @return Rack 리스트
     */
    public List<Rack> findAll() {
        try {
            // rack_mng 테이블 조회 (서버 개수 포함)
            String sql = "SELECT r.rm_seq, r.rm_user, r.rm_name, r.rm_order, r.rm_c_date, r.rm_use, " +
                        "       COALESCE(COUNT(s.sm_seq), 0) as server_count " +
                        "FROM rack_mng r " +
                        "LEFT JOIN server_mng s ON r.rm_seq = s.rm_seq " +
                        "WHERE 1=1 " +
                        "GROUP BY r.rm_seq, r.rm_user, r.rm_name, r.rm_order, r.rm_c_date, r.rm_use " +
                        "ORDER BY r.rm_order ASC";
            
            List<Rack> racks = jdbcTemplate.query(sql, new RackRowMapper());
            
            if (racks == null || racks.isEmpty()) {
                return new ArrayList<>();
            }
            
            return racks;
        } catch (Exception e) {
            throw new RuntimeException("Rack 테이블 조회 실패: " + e.getMessage() + 
                " (원인: " + e.getClass().getSimpleName() + ")", e);
        }
    }

    /**
     * Rack RowMapper
     */
    private static class RackRowMapper implements RowMapper<Rack> {
        @Override
        public Rack mapRow(ResultSet rs, int rowNum) throws SQLException {
            Rack rack = new Rack();
            
            // 동적으로 컬럼 읽기 (테이블 구조에 맞게)
            try {
                rack.setRSeq(rs.getInt("rm_seq"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                rack.setRUser(rs.getObject("rm_user") != null ? rs.getLong("rm_user") : null);
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                rack.setRName(rs.getString("rm_name"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                rack.setROrder(rs.getObject("rm_order") != null ? rs.getInt("rm_order") : null);
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                java.sql.Timestamp rCDate = rs.getTimestamp("rm_c_date");
                if (rCDate != null) {
                    rack.setRCDate(rCDate.toLocalDateTime());
                }
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                rack.setRmUse(rs.getString("rm_use"));
            } catch (SQLException e) {
                // 컬럼이 없으면 무시
            }
            
            try {
                rack.setServerCount(rs.getInt("server_count"));
            } catch (SQLException e) {
                // 컬럼이 없으면 0으로 설정
                rack.setServerCount(0);
            }
            
            return rack;
        }
    }
}

