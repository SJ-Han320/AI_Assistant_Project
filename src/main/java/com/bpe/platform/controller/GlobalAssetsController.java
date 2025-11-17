package com.bpe.platform.controller;

import com.bpe.platform.service.GlobalAssetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 글로벌 자산 동향 컨트롤러
 * ADMIN 권한만 접근 가능
 */
@RestController
@RequestMapping("/api/global-assets")
public class GlobalAssetsController {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalAssetsController.class);
    
    @Autowired
    private GlobalAssetsService globalAssetsService;
    
    @GetMapping("/bitcoin-usdt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBitcoinUSDT() {
        logger.info("비트코인 USDT 데이터 요청");
        Map<String, Object> result = globalAssetsService.getBitcoinUSDT();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/ethereum-usdt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEthereumUSDT() {
        logger.info("이더리움 USDT 데이터 요청");
        Map<String, Object> result = globalAssetsService.getEthereumUSDT();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/nasdaq")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNasdaq() {
        logger.info("나스닥 데이터 요청");
        Map<String, Object> result = globalAssetsService.getNasdaq();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/bitcoin-dominance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBitcoinDominance() {
        logger.info("비트 도미넌스 데이터 요청");
        Map<String, Object> result = globalAssetsService.getBitcoinDominance();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/gold")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getGold() {
        logger.info("금 가격 데이터 요청");
        Map<String, Object> result = globalAssetsService.getGoldPrice();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/dollar-index")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDollarIndex() {
        logger.info("달러 인덱스 데이터 요청");
        Map<String, Object> result = globalAssetsService.getDollarIndex();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllAssets() {
        logger.info("모든 자산 데이터 요청");
        Map<String, Object> result = new HashMap<>();
        
        result.put("bitcoinUsdt", globalAssetsService.getBitcoinUSDT());
        result.put("ethereumUsdt", globalAssetsService.getEthereumUSDT());
        result.put("nasdaq", globalAssetsService.getNasdaq());
        result.put("bitcoinDominance", globalAssetsService.getBitcoinDominance());
        result.put("gold", globalAssetsService.getGoldPrice());
        result.put("dollarIndex", globalAssetsService.getDollarIndex());
        
        return ResponseEntity.ok(result);
    }
}

