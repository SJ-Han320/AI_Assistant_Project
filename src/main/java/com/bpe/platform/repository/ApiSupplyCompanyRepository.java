package com.bpe.platform.repository;

import com.bpe.platform.entity.ApiSupplyCompany;
import com.bpe.platform.entity.ApiSupplyCompanyDetail;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ApiSupplyCompanyRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApiSupplyCompanyRepository(@Qualifier("dataSupplyApiJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 전체 개수 조회
     */
    public long countAll() {
        try {
            String sql = "SELECT COUNT(*) FROM STAGE_API_SUPPLY_COMPANY";
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
        } catch (Exception e) {
            throw new RuntimeException("STAGE_API_SUPPLY_COMPANY 테이블 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 필터별 개수 조회
     * @param filter 'closed', 'expiring', 'active', '' (전체)
     */
    public long countByFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            return countAll();
        }
        
        // 필터는 Service에서 계산하므로 전체 개수 반환
        // 실제로는 Service에서 필터링된 리스트의 개수를 반환
        return countAll();
    }

    /**
     * DB 연결 테스트
     */
    public boolean testConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 테이블 존재 여부 확인
     */
    public boolean tableExists() {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                         "WHERE table_schema = 'DATA_SUPPLY_API' AND table_name = 'STAGE_API_SUPPLY_COMPANY'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 테이블 컬럼 정보 조회 (디버깅용)
     */
    public List<String> getTableColumns() {
        try {
            String sql = "SELECT COLUMN_NAME FROM information_schema.columns " +
                         "WHERE table_schema = 'DATA_SUPPLY_API' AND table_name = 'STAGE_API_SUPPLY_COMPANY' " +
                         "ORDER BY ORDINAL_POSITION";
            return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("COLUMN_NAME"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 전체 조회 (페이징 없음)
     * @return 프로젝트 리스트
     */
    public List<ApiSupplyCompany> findAll() {
        try {
            // 먼저 연결 테스트
            if (!testConnection()) {
                throw new RuntimeException("데이터베이스 연결 실패");
            }
            
            // 테이블 존재 확인
            if (!tableExists()) {
                throw new RuntimeException("STAGE_API_SUPPLY_COMPANY 테이블이 존재하지 않습니다");
            }
            
            // 먼저 테이블 컬럼 확인
            List<String> columns = getTableColumns();
            if (columns.isEmpty()) {
                throw new RuntimeException("테이블 컬럼 정보를 가져올 수 없습니다");
            }
            
            // 기본 키 필드 찾기 (일반적인 필드명들 시도)
            String primaryKeyField = null;
            for (String col : columns) {
                if (col.equalsIgnoreCase("asc_seq") || col.equalsIgnoreCase("com_seq") || 
                    col.equalsIgnoreCase("id") || col.equalsIgnoreCase("seq")) {
                    primaryKeyField = col;
                    break;
                }
            }
            
            // 필수 필드 확인
            boolean hasUseYn = columns.stream().anyMatch(c -> c.equalsIgnoreCase("use_yn"));
            boolean hasComName = columns.stream().anyMatch(c -> c.equalsIgnoreCase("com_name"));
            boolean hasStartDate = columns.stream().anyMatch(c -> c.equalsIgnoreCase("start_date"));
            boolean hasEndDate = columns.stream().anyMatch(c -> c.equalsIgnoreCase("end_date"));
            boolean hasRegDate = columns.stream().anyMatch(c -> c.equalsIgnoreCase("reg_date"));
            
            if (!hasUseYn || !hasComName || !hasStartDate || !hasEndDate || !hasRegDate) {
                throw new RuntimeException("필수 컬럼이 없습니다. 실제 컬럼: " + columns);
            }
            
            // SELECT 쿼리 구성 (기본 키 필드가 있으면 포함)
            StringBuilder sqlBuilder = new StringBuilder("SELECT ");
            if (primaryKeyField != null) {
                sqlBuilder.append(primaryKeyField).append(", ");
            }
            sqlBuilder.append("use_yn, com_name, start_date, end_date, reg_date ");
            sqlBuilder.append("FROM STAGE_API_SUPPLY_COMPANY ");
            sqlBuilder.append("ORDER BY reg_date DESC");
            
            String sql = sqlBuilder.toString();
            
            List<ApiSupplyCompany> result = jdbcTemplate.query(sql, new ApiSupplyCompanyRowMapper());
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("STAGE_API_SUPPLY_COMPANY 테이블 조회 실패: " + e.getMessage() + 
                " (원인: " + e.getClass().getSimpleName() + ")", e);
        }
    }

    /**
     * 페이징 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 프로젝트 리스트
     */
    public List<ApiSupplyCompany> findAll(int page, int size) {
        int offset = page * size;
        // MySQL에서 LIMIT와 OFFSET에 바인딩 변수를 사용할 때 문제가 발생할 수 있으므로 직접 값을 넣음
        // 하지만 매우 큰 값은 문제가 될 수 있으므로, 전체 조회는 별도 메서드 사용
        if (size >= Integer.MAX_VALUE - 1000) {
            return findAll();
        }
            // 기본 키 필드 찾기
            List<String> columns = getTableColumns();
            String primaryKeyField = null;
            for (String col : columns) {
                if (col.equalsIgnoreCase("asc_seq") || col.equalsIgnoreCase("com_seq") || 
                    col.equalsIgnoreCase("id") || col.equalsIgnoreCase("seq")) {
                    primaryKeyField = col;
                    break;
                }
            }
            
            StringBuilder sqlBuilder = new StringBuilder("SELECT ");
            if (primaryKeyField != null) {
                sqlBuilder.append(primaryKeyField).append(", ");
            }
            sqlBuilder.append("use_yn, com_name, start_date, end_date, reg_date ");
            sqlBuilder.append("FROM STAGE_API_SUPPLY_COMPANY ");
            sqlBuilder.append("ORDER BY reg_date DESC ");
            sqlBuilder.append("LIMIT ").append(size).append(" OFFSET ").append(offset);
            
            String sql = sqlBuilder.toString();
        
        return jdbcTemplate.query(sql, new ApiSupplyCompanyRowMapper());
    }

    /**
     * RowMapper 구현
     */
    private static class ApiSupplyCompanyRowMapper implements RowMapper<ApiSupplyCompany> {
        @Override
        public ApiSupplyCompany mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiSupplyCompany company = new ApiSupplyCompany();
            
            // 기본 키 필드 찾기 (여러 가능성 시도)
            try {
                if (hasColumn(rs, "asc_seq")) {
                    company.setComSeq(rs.getInt("asc_seq"));
                } else if (hasColumn(rs, "com_seq")) {
                    company.setComSeq(rs.getInt("com_seq"));
                } else if (hasColumn(rs, "id")) {
                    company.setComSeq(rs.getInt("id"));
                } else if (hasColumn(rs, "seq")) {
                    company.setComSeq(rs.getInt("seq"));
                }
            } catch (SQLException e) {
                // 기본 키 필드가 없으면 null로 설정
                company.setComSeq(null);
            }
            
            company.setUseYn(rs.getString("use_yn"));
            company.setComName(rs.getString("com_name"));
            
            // 날짜 처리
            java.sql.Date startDate = rs.getDate("start_date");
            if (startDate != null) {
                company.setStartDate(startDate.toLocalDate());
            }
            
            java.sql.Date endDate = rs.getDate("end_date");
            if (endDate != null) {
                company.setEndDate(endDate.toLocalDate());
            }
            
            java.sql.Timestamp regDate = rs.getTimestamp("reg_date");
            if (regDate != null) {
                company.setRegDate(regDate.toLocalDateTime());
            }
            
            return company;
        }
        
        private boolean hasColumn(ResultSet rs, String columnName) {
            try {
                rs.findColumn(columnName);
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
    }

    /**
     * 프로젝트 상세 정보 조회 (3개 테이블 JOIN)
     * @param comSeq 프로젝트 시퀀스 (실제로는 asc_seq 또는 다른 기본 키 필드)
     * @return 프로젝트 상세 정보
     */
    public ApiSupplyCompanyDetail findDetailByComSeq(Integer comSeq) {
        // 실제 기본 키 필드명 찾기
        List<String> columns = getTableColumns();
        String primaryKeyField = null;
        for (String col : columns) {
            if (col.equalsIgnoreCase("asc_seq")) {
                primaryKeyField = "asc_seq";
                break;
            } else if (col.equalsIgnoreCase("com_seq")) {
                primaryKeyField = "com_seq";
                break;
            } else if (col.equalsIgnoreCase("id")) {
                primaryKeyField = "id";
                break;
            } else if (col.equalsIgnoreCase("seq")) {
                primaryKeyField = "seq";
                break;
            }
        }
        
        if (primaryKeyField == null) {
            throw new RuntimeException("기본 키 필드를 찾을 수 없습니다. 실제 컬럼: " + columns);
        }
        
        // STAGE_API_SUPPLY_COMPANY와 STAGE_API_SUPPLY_SYSTEM JOIN
        String sql = "SELECT " +
                     "c." + primaryKeyField + ", c.com_name, c.com_key, c.start_date, c.end_date, c.use_yn, c.reg_date, " +
                     "s.search_start_date, s.search_date_diff, s.update_search_date_yn, s.update_search_date_offset, " +
                     "s.search_byte_length, s.search_count_yn, s.daily_search_total_count, s.monthly_search_total_count, " +
                     "s.daily_total_count, s.monthly_total_count, s.command, s.komoran_yn " +
                     "FROM STAGE_API_SUPPLY_COMPANY c " +
                     "LEFT JOIN STAGE_API_SUPPLY_SYSTEM s ON c." + primaryKeyField + " = s." + primaryKeyField + " " +
                     "WHERE c." + primaryKeyField + " = ?";
        
        List<ApiSupplyCompanyDetail> details = jdbcTemplate.query(sql, new ApiSupplyCompanyDetailRowMapper(primaryKeyField), comSeq);
        
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("프로젝트를 찾을 수 없습니다. " + primaryKeyField + ": " + comSeq);
        }
        
        ApiSupplyCompanyDetail detail = details.get(0);
        
        // STAGE_API_SUPPLY_HOST 조회 (여러 개일 수 있음)
        String hostSql = "SELECT host FROM STAGE_API_SUPPLY_HOST WHERE " + primaryKeyField + " = ?";
        List<String> hosts = jdbcTemplate.query(hostSql, (rs, rowNum) -> rs.getString("host"), comSeq);
        detail.setHosts(hosts != null ? hosts : new ArrayList<>());
        
        return detail;
    }

    /**
     * 프로젝트 상세 정보 RowMapper
     */
    private static class ApiSupplyCompanyDetailRowMapper implements RowMapper<ApiSupplyCompanyDetail> {
        private final String primaryKeyField;
        
        public ApiSupplyCompanyDetailRowMapper(String primaryKeyField) {
            this.primaryKeyField = primaryKeyField;
        }
        
        @Override
        public ApiSupplyCompanyDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiSupplyCompanyDetail detail = new ApiSupplyCompanyDetail();
            
            // STAGE_API_SUPPLY_COMPANY
            detail.setComSeq(rs.getInt(primaryKeyField));
            detail.setComName(rs.getString("com_name"));
            detail.setComKey(rs.getString("com_key"));
            
            java.sql.Date startDate = rs.getDate("start_date");
            if (startDate != null) {
                detail.setStartDate(startDate.toLocalDate());
            }
            
            java.sql.Date endDate = rs.getDate("end_date");
            if (endDate != null) {
                detail.setEndDate(endDate.toLocalDate());
            }
            
            detail.setUseYn(rs.getString("use_yn"));
            
            java.sql.Timestamp regDate = rs.getTimestamp("reg_date");
            if (regDate != null) {
                detail.setRegDate(regDate.toLocalDateTime());
            }
            
            // STAGE_API_SUPPLY_SYSTEM
            Integer searchStartDate = rs.getObject("search_start_date", Integer.class);
            detail.setSearchStartDate(searchStartDate);
            
            Integer searchDateDiff = rs.getObject("search_date_diff", Integer.class);
            detail.setSearchDateDiff(searchDateDiff);
            
            detail.setUpdateSearchDateYn(rs.getString("update_search_date_yn"));
            
            Integer updateSearchDateOffset = rs.getObject("update_search_date_offset", Integer.class);
            detail.setUpdateSearchDateOffset(updateSearchDateOffset);
            
            Integer searchByteLength = rs.getObject("search_byte_length", Integer.class);
            detail.setSearchByteLength(searchByteLength);
            
            detail.setSearchCountYn(rs.getString("search_count_yn"));
            
            Integer dailySearchTotalCount = rs.getObject("daily_search_total_count", Integer.class);
            detail.setDailySearchTotalCount(dailySearchTotalCount);
            
            Integer monthlySearchTotalCount = rs.getObject("monthly_search_total_count", Integer.class);
            detail.setMonthlySearchTotalCount(monthlySearchTotalCount);
            
            Integer dailyTotalCount = rs.getObject("daily_total_count", Integer.class);
            detail.setDailyTotalCount(dailyTotalCount);
            
            Integer monthlyTotalCount = rs.getObject("monthly_total_count", Integer.class);
            detail.setMonthlyTotalCount(monthlyTotalCount);
            
            detail.setCommand(rs.getString("command"));
            detail.setKomoranYn(rs.getString("komoran_yn"));
            
            return detail;
        }
    }
}

