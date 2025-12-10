package com.bpe.platform.service;

import com.bpe.platform.entity.Rack;
import com.bpe.platform.repository.RackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RackService {

    private static final Logger logger = LoggerFactory.getLogger(RackService.class);

    @Autowired
    private RackRepository rackRepository;

    /**
     * 전체 Rack 목록 조회
     * @return Rack 리스트
     */
    public List<Rack> getAllRacks() {
        logger.info("Rack 목록 조회 요청");
        try {
            List<Rack> racks = rackRepository.findAll();
            logger.info("Rack 목록 조회 완료: {}개", racks.size());
            return racks;
        } catch (Exception e) {
            logger.error("Rack 목록 조회 중 오류 발생", e);
            throw new RuntimeException("Rack 데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}

