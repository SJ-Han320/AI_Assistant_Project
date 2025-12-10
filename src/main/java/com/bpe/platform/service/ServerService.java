package com.bpe.platform.service;

import com.bpe.platform.entity.Server;
import com.bpe.platform.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 서버 관리 서비스
 */
@Service
public class ServerService {

    @Autowired
    private ServerRepository serverRepository;

    /**
     * 특정 Rack에 속한 서버 목록 조회
     * @param rmSeq Rack 번호
     * @return 서버 리스트
     */
    public List<Server> getServersByRmSeq(Integer rmSeq) {
        return serverRepository.findByRmSeq(rmSeq);
    }
}

