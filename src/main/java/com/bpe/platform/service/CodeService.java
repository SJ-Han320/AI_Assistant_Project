package com.bpe.platform.service;

import com.bpe.platform.entity.Code;
import com.bpe.platform.repository.CodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeService {
    
    @Autowired
    private CodeRepository codeRepository;
    
    public List<Code> getEsFields() {
        return codeRepository.findByCTypeAndCUseOrderByCOrderAsc("es_field");
    }
    
    public List<Code> getFieldList() {
        return codeRepository.findByCTypeAndCUseOrderByCOrderAsc("es_field", "Y");
    }
    
    public List<Code> getTagColors() {
        return codeRepository.findByCTypeAndCUseOrderByCOrderAsc("tag_color", "Y");
    }
}
