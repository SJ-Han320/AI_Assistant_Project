package com.bpe.platform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * ServerIssue 테이블 DTO
 */
public class ServerIssue {
    
    @JsonProperty("siSeq")
    private Integer siSeq;          // si_seq 필드 (서버이슈번호)
    
    @JsonProperty("smSeq")
    private Integer smSeq;          // sm_seq 필드 (서버 seq)
    
    @JsonProperty("siIssue")
    private String siIssue;         // si_issue 필드 (서버 이슈)
    
    @JsonProperty("siUser")
    private Long siUser;            // si_user 필드 (생성 및 수정자)
    
    @JsonProperty("siDate")
    private LocalDateTime siDate;   // si_date 필드 (생성 및 수정 일시)
    
    public ServerIssue() {
    }
    
    public Integer getSiSeq() {
        return siSeq;
    }
    
    public void setSiSeq(Integer siSeq) {
        this.siSeq = siSeq;
    }
    
    public Integer getSmSeq() {
        return smSeq;
    }
    
    public void setSmSeq(Integer smSeq) {
        this.smSeq = smSeq;
    }
    
    public String getSiIssue() {
        return siIssue;
    }
    
    public void setSiIssue(String siIssue) {
        this.siIssue = siIssue;
    }
    
    public Long getSiUser() {
        return siUser;
    }
    
    public void setSiUser(Long siUser) {
        this.siUser = siUser;
    }
    
    public LocalDateTime getSiDate() {
        return siDate;
    }
    
    public void setSiDate(LocalDateTime siDate) {
        this.siDate = siDate;
    }
}

