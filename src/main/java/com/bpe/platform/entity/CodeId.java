package com.bpe.platform.entity;

import java.io.Serializable;
import java.util.Objects;

public class CodeId implements Serializable {
    
    private String cType;
    private String cValue;
    
    // 기본 생성자
    public CodeId() {}
    
    // 생성자
    public CodeId(String cType, String cValue) {
        this.cType = cType;
        this.cValue = cValue;
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
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeId codeId = (CodeId) o;
        return Objects.equals(cType, codeId.cType) && Objects.equals(cValue, codeId.cValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cType, cValue);
    }
}
