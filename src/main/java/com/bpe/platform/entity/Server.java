package com.bpe.platform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Server 테이블 DTO
 */
public class Server {
    
    @JsonProperty("smSeq")
    private Integer smSeq;          // sm_seq 필드 (서버번호)
    
    @JsonProperty("rmSeq")
    private Integer rmSeq;          // rm_seq 필드 (소속 rack seq)
    
    @JsonProperty("smUser")
    private Long smUser;            // sm_user 필드 (생성자)
    
    @JsonProperty("smName")
    private String smName;          // sm_name 필드 (서버명)
    
    @JsonProperty("smModel")
    private String smModel;         // sm_model 필드 (MODEL)
    
    @JsonProperty("smCpu")
    private String smCpu;           // sm_cpu 필드 (CPU)
    
    @JsonProperty("smMem")
    private String smMem;           // sm_mem 필드 (MEMORY)
    
    @JsonProperty("smHdd")
    private String smHdd;           // sm_hdd 필드 (HDD)
    
    @JsonProperty("smOrder")
    private Integer smOrder;         // sm_order 필드
    
    @JsonProperty("smCDate")
    private LocalDateTime smCDate;   // sm_c_date 필드 (생성일시)
    
    @JsonProperty("smOs")
    private String smOs;             // sm_os 필드 (OS 정보)
    
    @JsonProperty("smMain")
    private String smMain;           // sm_main 필드 (메인 담당자)
    
    @JsonProperty("smSub")
    private String smSub;            // sm_sub 필드 (보조 담당자)
    
    @JsonProperty("smIssue")
    private String smIssue;          // sm_issue 필드 (서버 이슈)
    
    public Server() {
    }
    
    public Integer getSmSeq() {
        return smSeq;
    }
    
    public void setSmSeq(Integer smSeq) {
        this.smSeq = smSeq;
    }
    
    public Integer getRmSeq() {
        return rmSeq;
    }
    
    public void setRmSeq(Integer rmSeq) {
        this.rmSeq = rmSeq;
    }
    
    public Long getSmUser() {
        return smUser;
    }
    
    public void setSmUser(Long smUser) {
        this.smUser = smUser;
    }
    
    public String getSmName() {
        return smName;
    }
    
    public void setSmName(String smName) {
        this.smName = smName;
    }
    
    public String getSmModel() {
        return smModel;
    }
    
    public void setSmModel(String smModel) {
        this.smModel = smModel;
    }
    
    public String getSmCpu() {
        return smCpu;
    }
    
    public void setSmCpu(String smCpu) {
        this.smCpu = smCpu;
    }
    
    public String getSmMem() {
        return smMem;
    }
    
    public void setSmMem(String smMem) {
        this.smMem = smMem;
    }
    
    public String getSmHdd() {
        return smHdd;
    }
    
    public void setSmHdd(String smHdd) {
        this.smHdd = smHdd;
    }
    
    public Integer getSmOrder() {
        return smOrder;
    }
    
    public void setSmOrder(Integer smOrder) {
        this.smOrder = smOrder;
    }
    
    public LocalDateTime getSmCDate() {
        return smCDate;
    }
    
    public void setSmCDate(LocalDateTime smCDate) {
        this.smCDate = smCDate;
    }
    
    public String getSmOs() {
        return smOs;
    }
    
    public void setSmOs(String smOs) {
        this.smOs = smOs;
    }
    
    public String getSmMain() {
        return smMain;
    }
    
    public void setSmMain(String smMain) {
        this.smMain = smMain;
    }
    
    public String getSmSub() {
        return smSub;
    }
    
    public void setSmSub(String smSub) {
        this.smSub = smSub;
    }
    
    public String getSmIssue() {
        return smIssue;
    }
    
    public void setSmIssue(String smIssue) {
        this.smIssue = smIssue;
    }
}

