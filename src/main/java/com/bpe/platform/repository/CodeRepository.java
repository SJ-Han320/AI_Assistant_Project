package com.bpe.platform.repository;

import com.bpe.platform.entity.Code;
import com.bpe.platform.entity.CodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<Code, CodeId> {
    
    @Query("SELECT c FROM Code c WHERE c.cType = :cType AND c.cUse = 'Y' ORDER BY c.cOrder ASC")
    List<Code> findByCTypeAndCUseOrderByCOrderAsc(@Param("cType") String cType);
    
    @Query("SELECT c FROM Code c WHERE c.cType = :cType AND c.cUse = :cUse ORDER BY c.cOrder ASC")
    List<Code> findByCTypeAndCUseOrderByCOrderAsc(@Param("cType") String cType, @Param("cUse") String cUse);
}
