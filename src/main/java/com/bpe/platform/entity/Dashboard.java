package com.bpe.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "dashboard")
public class Dashboard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "d_seq")
    private Integer dSeq;
    
    @Column(name = "d_user")
    private Long dUser;
    
    @Column(name = "d_name")
    private String dName;
    
    @Column(name = "d_description")
    private String dDescription;
    
    @Column(name = "d_icon")
    private String dIcon;
    
    @Column(name = "d_url")
    private String dUrl;
    
    @Column(name = "d_order")
    private Integer dOrder;
    
    @Column(name = "d_date")
    private LocalDateTime dDate;
    
    // User와의 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "d_user", insertable = false, updatable = false)
    private User user;
    
    // 기본 생성자
    public Dashboard() {}
    
    @PrePersist
    protected void onCreate() {
        if (dDate == null) {
            // 한국 시간(KST)으로 설정
            dDate = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
    }
    
    // Getters and Setters
    public Integer getDSeq() {
        return dSeq;
    }
    
    public void setDSeq(Integer dSeq) {
        this.dSeq = dSeq;
    }
    
    public Long getDUser() {
        return dUser;
    }
    
    public void setDUser(Long dUser) {
        this.dUser = dUser;
    }
    
    public String getDName() {
        return dName;
    }
    
    public void setDName(String dName) {
        this.dName = dName;
    }
    
    public String getDDescription() {
        return dDescription;
    }
    
    public void setDDescription(String dDescription) {
        this.dDescription = dDescription;
    }
    
    public String getDIcon() {
        return dIcon;
    }
    
    public void setDIcon(String dIcon) {
        this.dIcon = dIcon;
    }
    
    public String getDUrl() {
        return dUrl;
    }
    
    public void setDUrl(String dUrl) {
        this.dUrl = dUrl;
    }
    
    public Integer getDOrder() {
        return dOrder;
    }
    
    public void setDOrder(Integer dOrder) {
        this.dOrder = dOrder;
    }
    
    public LocalDateTime getDDate() {
        return dDate;
    }
    
    public void setDDate(LocalDateTime dDate) {
        this.dDate = dDate;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}

