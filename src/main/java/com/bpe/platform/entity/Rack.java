package com.bpe.platform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Rack 테이블 DTO
 */
public class Rack {
    
    @JsonProperty("rSeq")
    private Integer rSeq;          // r_seq 필드 (렉번호)
    
    @JsonProperty("rUser")
    private Long rUser;            // r_user 필드 (생성자)
    
    @JsonProperty("rName")
    private String rName;          // r_name 필드 (렉명)
    
    @JsonProperty("rOrder")
    private Integer rOrder;        // r_order 필드
    
    @JsonProperty("rCDate")
    private LocalDateTime rCDate;  // r_c_date 필드 (생성일시)
    
    @JsonProperty("rmUse")
    private String rmUse;          // rm_use 필드 (우리팀사용여부)
    
    @JsonProperty("serverCount")
    private Integer serverCount;   // 서버 대수 (서버 테이블과 조인해서 계산)
    
    public Rack() {
    }
    
    public Integer getRSeq() {
        return rSeq;
    }
    
    public void setRSeq(Integer rSeq) {
        this.rSeq = rSeq;
    }
    
    public Long getRUser() {
        return rUser;
    }
    
    public void setRUser(Long rUser) {
        this.rUser = rUser;
    }
    
    public String getRName() {
        return rName;
    }
    
    public void setRName(String rName) {
        this.rName = rName;
    }
    
    public Integer getROrder() {
        return rOrder;
    }
    
    public void setROrder(Integer rOrder) {
        this.rOrder = rOrder;
    }
    
    public LocalDateTime getRCDate() {
        return rCDate;
    }
    
    public void setRCDate(LocalDateTime rCDate) {
        this.rCDate = rCDate;
    }
    
    public String getRmUse() {
        return rmUse;
    }
    
    public void setRmUse(String rmUse) {
        this.rmUse = rmUse;
    }
    
    public Integer getServerCount() {
        return serverCount;
    }
    
    public void setServerCount(Integer serverCount) {
        this.serverCount = serverCount;
    }
}

