package com.bpe.platform.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "spark_task")
public class SparkTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "st_seq")
    private Integer stSeq;
    
    @Column(name = "st_name")
    private String stName;
    
    @Column(name = "st_str_date")
    private LocalDateTime stStrDate;
    
    @Column(name = "st_end_date")
    private LocalDateTime stEndDate;
    
    @Column(name = "st_progress")
    private Integer stProgress;
    
    @Column(name = "st_user")
    private Long stUser;
    
    // User와의 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "st_user", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    
    @Column(name = "st_status")
    private String stStatus;
    
    @Column(name = "st_query")
    private String stQuery;
    
    @Column(name = "st_host")
    private String stHost;
    
    @Column(name = "st_db")
    private String stDb;
    
    @Column(name = "st_destination")
    private String stDestination;
    
    @Column(name = "st_db_id")
    private String stDbId;
    
    @Column(name = "st_db_pw")
    private String stDbPw;
    
    @Column(name = "st_field")
    private String stField;
    
    @Column(name = "st_type")
    private String stType;
    
    // 기본 생성자
    public SparkTask() {}
    
    // 생성자
    public SparkTask(String stName, Long stUser, String stStatus, String stQuery) {
        this.stName = stName;
        this.stUser = stUser;
        this.stStatus = stStatus;
        this.stQuery = stQuery;
        this.stProgress = 0; // 기본값 0
        this.stStrDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getStSeq() {
        return stSeq;
    }
    
    public void setStSeq(Integer stSeq) {
        this.stSeq = stSeq;
    }
    
    public String getStName() {
        return stName;
    }
    
    public void setStName(String stName) {
        this.stName = stName;
    }
    
    public LocalDateTime getStStrDate() {
        return stStrDate;
    }
    
    public void setStStrDate(LocalDateTime stStrDate) {
        this.stStrDate = stStrDate;
    }
    
    public LocalDateTime getStEndDate() {
        return stEndDate;
    }
    
    public void setStEndDate(LocalDateTime stEndDate) {
        this.stEndDate = stEndDate;
    }
    
    public Integer getStProgress() {
        return stProgress;
    }
    
    public void setStProgress(Integer stProgress) {
        this.stProgress = stProgress;
    }
    
    public Long getStUser() {
        return stUser;
    }

    public void setStUser(Long stUser) {
        this.stUser = stUser;
    }
    
    public String getStStatus() {
        return stStatus;
    }
    
    public void setStStatus(String stStatus) {
        this.stStatus = stStatus;
    }
    
    public String getStQuery() {
        return stQuery;
    }
    
    public void setStQuery(String stQuery) {
        this.stQuery = stQuery;
    }
    
    public String getStHost() {
        return stHost;
    }
    
    public void setStHost(String stHost) {
        this.stHost = stHost;
    }
    
    public String getStDb() {
        return stDb;
    }
    
    public void setStDb(String stDb) {
        this.stDb = stDb;
    }
    
    public String getStDestination() {
        return stDestination;
    }
    
    public void setStDestination(String stDestination) {
        this.stDestination = stDestination;
    }
    
    public String getStDbId() {
        return stDbId;
    }
    
    public void setStDbId(String stDbId) {
        this.stDbId = stDbId;
    }
    
    public String getStDbPw() {
        return stDbPw;
    }
    
    public void setStDbPw(String stDbPw) {
        this.stDbPw = stDbPw;
    }
    
    public String getStField() {
        return stField;
    }
    
    public void setStField(String stField) {
        this.stField = stField;
    }
    
    public String getStType() {
        return stType;
    }
    
    public void setStType(String stType) {
        this.stType = stType;
    }
    
    // User 관계 getter/setter
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // 편의 메서드 (기존 코드 호환성을 위해)
    public String getPercent() {
        return stProgress + "%";
    }
    
    // 상태 코드를 한글로 변환하는 메서드
    public String getStatusDisplayName() {
        switch (stStatus) {
            case "S": return "진행";
            case "C": return "완료";
            case "W": return "대기";
            case "E": return "오류";
            default: return stStatus;
        }
    }
    
    // 상태 코드에 따른 이모지 반환
    public String getStatusEmoji() {
        switch (stStatus) {
            case "S": return "🚀";
            case "C": return "✅";
            case "W": return "⏳";
            case "E": return "🚧";
            default: return "❓";
        }
    }
    
    // 상태 코드에 따른 텍스트 반환
    public String getStatusText() {
        switch (stStatus) {
            case "S": return "진행";
            case "C": return "완료";
            case "W": return "대기";
            case "E": return "오류";
            default: return "알수없음";
        }
    }
    
    // 상태 이모지와 텍스트를 함께 반환
    public String getStatusDisplay() {
        return getStatusEmoji() + " " + getStatusText();
    }
    
    // 기존 코드 호환성을 위한 메서드들
    public Integer getSeq() {
        return stSeq;
    }
    
    public void setSeq(Integer seq) {
        this.stSeq = seq;
    }
    
    public String getStatus() {
        return stStatus;
    }
    
    public void setStatus(String status) {
        this.stStatus = status;
    }
    
    public String getProjectName() {
        return stName;
    }
    
    public void setProjectName(String projectName) {
        this.stName = projectName;
    }
    
    public Integer getProgress() {
        return stProgress;
    }
    
    public void setProgress(Integer progress) {
        this.stProgress = progress;
    }
    
    public String getUserName() {
        return user != null ? user.getName() : "N/A";
    }
    
    public void setUserName(String userName) {
        this.stUser = userName != null ? Long.parseLong(userName) : null;
    }
    
    public LocalDateTime getCreatedAt() {
        return stStrDate;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.stStrDate = createdAt;
    }
}