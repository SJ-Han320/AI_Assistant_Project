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
    
    // 메타데이터 캐싱 (성능 최적화)
    private String cachedPrimaryKeyField = null;
    private static final Object metadataLock = new Object();

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
     * 기본 키 필드명 가져오기 (캐싱)
     */
    private String getPrimaryKeyFieldCached() {
        synchronized (metadataLock) {
            if (cachedPrimaryKeyField != null) {
                return cachedPrimaryKeyField;
            }
            
            List<String> columns = getTableColumns();
            for (String col : columns) {
                if (col.equalsIgnoreCase("asc_seq")) {
                    cachedPrimaryKeyField = "asc_seq";
                    return cachedPrimaryKeyField;
                } else if (col.equalsIgnoreCase("com_seq")) {
                    cachedPrimaryKeyField = "com_seq";
                    return cachedPrimaryKeyField;
                } else if (col.equalsIgnoreCase("id")) {
                    cachedPrimaryKeyField = "id";
                    return cachedPrimaryKeyField;
                } else if (col.equalsIgnoreCase("seq")) {
                    cachedPrimaryKeyField = "seq";
                    return cachedPrimaryKeyField;
                }
            }
            throw new RuntimeException("기본 키 필드를 찾을 수 없습니다. 실제 컬럼: " + columns);
        }
    }
    
    /**
     * 전체 조회 (페이징 없음) - 최적화 버전
     * @return 프로젝트 리스트
     */
    public List<ApiSupplyCompany> findAll() {
        try {
            // 메타데이터는 캐싱된 값 사용 (첫 호출 시에만 조회)
            String primaryKeyField = getPrimaryKeyFieldCached();
            
            // SELECT 쿼리 구성
            StringBuilder sqlBuilder = new StringBuilder("SELECT ");
            sqlBuilder.append(primaryKeyField).append(", ");
            sqlBuilder.append("use_yn, com_name, start_date, end_date, reg_date ");
            sqlBuilder.append("FROM STAGE_API_SUPPLY_COMPANY ");
            sqlBuilder.append("ORDER BY reg_date DESC");
            
            String sql = sqlBuilder.toString();
            
            List<ApiSupplyCompany> result = jdbcTemplate.query(sql, new ApiSupplyCompanyRowMapper());
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            // 에러 발생 시 캐시 초기화
            synchronized (metadataLock) {
                cachedPrimaryKeyField = null;
            }
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

    /**
     * 기본 키 필드명 찾기
     */
    private String getPrimaryKeyField() {
        List<String> columns = getTableColumns();
        for (String col : columns) {
            if (col.equalsIgnoreCase("asc_seq")) {
                return "asc_seq";
            } else if (col.equalsIgnoreCase("com_seq")) {
                return "com_seq";
            } else if (col.equalsIgnoreCase("id")) {
                return "id";
            } else if (col.equalsIgnoreCase("seq")) {
                return "seq";
            }
        }
        throw new RuntimeException("기본 키 필드를 찾을 수 없습니다. 실제 컬럼: " + columns);
    }

    /**
     * 프로젝트 추가 (3개 테이블에 INSERT)
     * @param detail 프로젝트 상세 정보
     * @return 생성된 asc_seq 값
     */
    public Integer createProject(ApiSupplyCompanyDetail detail) {
        String primaryKeyField = getPrimaryKeyField();
        
        try {
            // 1. STAGE_API_SUPPLY_COMPANY에 INSERT
            String companySql = "INSERT INTO STAGE_API_SUPPLY_COMPANY (com_name, com_key, start_date, end_date, use_yn, reg_date) " +
                               "VALUES (?, ?, ?, ?, 'Y', NOW())";
            
            jdbcTemplate.update(companySql,
                detail.getComName(),
                detail.getComKey(),
                detail.getStartDate() != null ? java.sql.Date.valueOf(detail.getStartDate()) : null,
                detail.getEndDate() != null ? java.sql.Date.valueOf(detail.getEndDate()) : null
            );
            
            // 2. 생성된 asc_seq 가져오기
            String getSeqSql = "SELECT " + primaryKeyField + " FROM STAGE_API_SUPPLY_COMPANY WHERE com_key = ? ORDER BY reg_date DESC LIMIT 1";
            Integer ascSeq = jdbcTemplate.queryForObject(getSeqSql, Integer.class, detail.getComKey());
            
            if (ascSeq == null) {
                throw new RuntimeException("프로젝트 생성 후 asc_seq를 가져올 수 없습니다.");
            }
            
            // 3. STAGE_API_SUPPLY_SYSTEM에 INSERT
            String systemSql = "INSERT INTO STAGE_API_SUPPLY_SYSTEM (" + primaryKeyField + ", search_start_date, search_date_diff, " +
                              "update_search_date_yn, update_search_date_offset, search_byte_length, search_count_yn, " +
                              "daily_search_total_count, monthly_search_total_count, daily_total_count, monthly_total_count, " +
                              "command, komoran_yn) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(systemSql,
                ascSeq,
                detail.getSearchStartDate(),
                detail.getSearchDateDiff(),
                detail.getUpdateSearchDateYn(),
                detail.getUpdateSearchDateOffset(),
                detail.getSearchByteLength(),
                detail.getSearchCountYn(),
                detail.getDailySearchTotalCount(),
                detail.getMonthlySearchTotalCount(),
                detail.getDailyTotalCount(),
                detail.getMonthlyTotalCount(),
                detail.getCommand(),
                detail.getKomoranYn()
            );
            
            // 4. STAGE_API_SUPPLY_HOST에 INSERT (여러 개)
            if (detail.getHosts() != null && !detail.getHosts().isEmpty()) {
                String hostSql = "INSERT INTO STAGE_API_SUPPLY_HOST (" + primaryKeyField + ", host) VALUES (?, ?)";
                for (String host : detail.getHosts()) {
                    if (host != null && !host.trim().isEmpty()) {
                        jdbcTemplate.update(hostSql, ascSeq, host.trim());
                    }
                }
            }
            
            return ascSeq;
        } catch (Exception e) {
            throw new RuntimeException("프로젝트 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 프로젝트의 use_yn을 'E'로 업데이트 (오류 상태)
     * @param comKey 프로젝트 고유키
     */
    public void updateUseYnToErrorByComKey(String comKey) {
        try {
            String sql = "UPDATE STAGE_API_SUPPLY_COMPANY SET use_yn = 'E' WHERE com_key = ?";
            jdbcTemplate.update(sql, comKey);
        } catch (Exception e) {
            throw new RuntimeException("프로젝트 상태 업데이트 실패: " + e.getMessage(), e);
        }
    }
}

