package com.bpe.platform.service;

import com.bpe.platform.entity.ServerIssue;
import com.bpe.platform.repository.ServerIssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 서버 이슈 서비스
 */
@Service
public class ServerIssueService {

    @Autowired
    private ServerIssueRepository serverIssueRepository;

    /**
     * 특정 서버의 이슈 목록 조회
     * @param smSeq 서버 번호
     * @return 서버 이슈 리스트
     */
    public List<ServerIssue> getIssuesBySmSeq(Integer smSeq) {
        return serverIssueRepository.findBySmSeq(smSeq);
    }
}

