package com.bpe.platform.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * API_SUPPLY_COMPANY 테이블 DTO
 * DB 테이블은 절대 수정하지 않음
 */
public class ApiSupplyCompany {
    
    private Integer comSeq;      // com_seq 필드 (프로젝트 클릭 시 사용)
    private String useYn;        // use_yn 필드
    private String comName;       // com_name 필드
    private LocalDate startDate;  // start_date 필드
    private LocalDate endDate;    // end_date 필드
    private LocalDateTime regDate; // reg_date 필드
    
    // 상태 계산 필드 (DB에 없음, 계산으로 생성)
    private String status;       // '종료', '운영', '종료 임박'
    
    public ApiSupplyCompany() {
    }
    
    public Integer getComSeq() {
        return comSeq;
    }
    
    public void setComSeq(Integer comSeq) {
        this.comSeq = comSeq;
    }
    
    public String getUseYn() {
        return useYn;
    }
    
    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }
    
    public String getComName() {
        return comName;
    }
    
    public void setComName(String comName) {
        this.comName = comName;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LocalDateTime getRegDate() {
        return regDate;
    }
    
    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}

