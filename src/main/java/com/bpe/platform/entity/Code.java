package com.bpe.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "code")
@IdClass(CodeId.class)
public class Code {
    
    @Id
    @Column(name = "c_type")
    private String cType;
    
    @Id
    @Column(name = "c_value")
    private String cValue;
    
    @Column(name = "c_name")
    private String cName;
    
    @Column(name = "c_order")
    private Integer cOrder;
    
    @Column(name = "c_use")
    private String cUse;
    
    // 기본 생성자
    public Code() {}
    
    // 생성자
    public Code(String cType, String cValue, String cName, Integer cOrder, String cUse) {
        this.cType = cType;
        this.cValue = cValue;
        this.cName = cName;
        this.cOrder = cOrder;
        this.cUse = cUse;
    }
    
    // Getters and Setters
    
    public String getCType() {
        return cType;
    }
    
    public void setCType(String cType) {
        this.cType = cType;
    }
    
    public String getCValue() {
        return cValue;
    }
    
    public void setCValue(String cValue) {
        this.cValue = cValue;
    }
    
    public String getCName() {
        return cName;
    }
    
    public void setCName(String cName) {
        this.cName = cName;
    }
    
    public Integer getCOrder() {
        return cOrder;
    }
    
    public void setCOrder(Integer cOrder) {
        this.cOrder = cOrder;
    }
    
    public String getCUse() {
        return cUse;
    }
    
    public void setCUse(String cUse) {
        this.cUse = cUse;
    }
}
