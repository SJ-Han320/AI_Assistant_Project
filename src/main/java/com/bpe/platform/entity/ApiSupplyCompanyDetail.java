package com.bpe.platform.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로젝트 상세 정보 DTO (3개 테이블 JOIN 결과)
 */
public class ApiSupplyCompanyDetail {
    
    // STAGE_API_SUPPLY_COMPANY
    private Integer comSeq;
    private String comName;
    private String comKey;
    private LocalDate startDate;
    private LocalDate endDate;
    private String useYn;
    private LocalDateTime regDate;
    
    // STAGE_API_SUPPLY_SYSTEM
    private Integer searchStartDate;
    private Integer searchDateDiff;
    private String updateSearchDateYn;
    private Integer updateSearchDateOffset;
    private Integer searchByteLength;
    private String searchCountYn;
    private Integer dailySearchTotalCount;
    private Integer monthlySearchTotalCount;
    private Integer dailyTotalCount;
    private Integer monthlyTotalCount;
    private String command;
    private String komoranYn;
    
    // STAGE_API_SUPPLY_HOST (여러 개일 수 있음)
    private List<String> hosts;
    
    public ApiSupplyCompanyDetail() {
    }
    
    // Getters and Setters
    public Integer getComSeq() {
        return comSeq;
    }
    
    public void setComSeq(Integer comSeq) {
        this.comSeq = comSeq;
    }
    
    public String getComName() {
        return comName;
    }
    
    public void setComName(String comName) {
        this.comName = comName;
    }
    
    public String getComKey() {
        return comKey;
    }
    
    public void setComKey(String comKey) {
        this.comKey = comKey;
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
    
    public String getUseYn() {
        return useYn;
    }
    
    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }
    
    public LocalDateTime getRegDate() {
        return regDate;
    }
    
    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }
    
    public Integer getSearchStartDate() {
        return searchStartDate;
    }
    
    public void setSearchStartDate(Integer searchStartDate) {
        this.searchStartDate = searchStartDate;
    }
    
    public Integer getSearchDateDiff() {
        return searchDateDiff;
    }
    
    public void setSearchDateDiff(Integer searchDateDiff) {
        this.searchDateDiff = searchDateDiff;
    }
    
    public String getUpdateSearchDateYn() {
        return updateSearchDateYn;
    }
    
    public void setUpdateSearchDateYn(String updateSearchDateYn) {
        this.updateSearchDateYn = updateSearchDateYn;
    }
    
    public Integer getUpdateSearchDateOffset() {
        return updateSearchDateOffset;
    }
    
    public void setUpdateSearchDateOffset(Integer updateSearchDateOffset) {
        this.updateSearchDateOffset = updateSearchDateOffset;
    }
    
    public Integer getSearchByteLength() {
        return searchByteLength;
    }
    
    public void setSearchByteLength(Integer searchByteLength) {
        this.searchByteLength = searchByteLength;
    }
    
    public String getSearchCountYn() {
        return searchCountYn;
    }
    
    public void setSearchCountYn(String searchCountYn) {
        this.searchCountYn = searchCountYn;
    }
    
    public Integer getDailySearchTotalCount() {
        return dailySearchTotalCount;
    }
    
    public void setDailySearchTotalCount(Integer dailySearchTotalCount) {
        this.dailySearchTotalCount = dailySearchTotalCount;
    }
    
    public Integer getMonthlySearchTotalCount() {
        return monthlySearchTotalCount;
    }
    
    public void setMonthlySearchTotalCount(Integer monthlySearchTotalCount) {
        this.monthlySearchTotalCount = monthlySearchTotalCount;
    }
    
    public Integer getDailyTotalCount() {
        return dailyTotalCount;
    }
    
    public void setDailyTotalCount(Integer dailyTotalCount) {
        this.dailyTotalCount = dailyTotalCount;
    }
    
    public Integer getMonthlyTotalCount() {
        return monthlyTotalCount;
    }
    
    public void setMonthlyTotalCount(Integer monthlyTotalCount) {
        this.monthlyTotalCount = monthlyTotalCount;
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public String getKomoranYn() {
        return komoranYn;
    }
    
    public void setKomoranYn(String komoranYn) {
        this.komoranYn = komoranYn;
    }
    
    public List<String> getHosts() {
        return hosts;
    }
    
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }
}

