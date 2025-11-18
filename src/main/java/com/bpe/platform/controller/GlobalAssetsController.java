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
    public ResponseEntity<Map<String, Object>> getBitcoinUSDT(@RequestParam(required = false, defaultValue = "1month") String period) {
        logger.info("비트코인 USDT 데이터 요청 - period: {}", period);
        Map<String, Object> result = globalAssetsService.getBitcoinUSDT(period);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/ethereum-usdt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEthereumUSDT(@RequestParam(required = false, defaultValue = "1month") String period) {
        logger.info("이더리움 USDT 데이터 요청 - period: {}", period);
        Map<String, Object> result = globalAssetsService.getEthereumUSDT(period);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/nasdaq")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNasdaq(@RequestParam(required = false, defaultValue = "1month") String period) {
        logger.info("나스닥 데이터 요청 - period: {}", period);
        Map<String, Object> result = globalAssetsService.getNasdaq(period);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/bitcoin-dominance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBitcoinDominance(@RequestParam(required = false, defaultValue = "1month") String period) {
        logger.info("비트 도미넌스 데이터 요청 - period: {}", period);
        Map<String, Object> result = globalAssetsService.getBitcoinDominance(period);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/gold")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getGold(@RequestParam(required = false, defaultValue = "1month") String period) {
        logger.info("금 가격 데이터 요청 - period: {}", period);
        Map<String, Object> result = globalAssetsService.getGoldPrice(period);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/dollar-index")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDollarIndex(@RequestParam(required = false, defaultValue = "1month") String period) {
        logger.info("달러 인덱스 데이터 요청 - period: {}", period);
        Map<String, Object> result = globalAssetsService.getDollarIndex(period);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllAssets(@RequestParam(required = false, defaultValue = "1month") String period) {
        logger.info("모든 자산 데이터 요청 - period: {}", period);
        Map<String, Object> result = new HashMap<>();
        
        result.put("bitcoinUsdt", globalAssetsService.getBitcoinUSDT(period));
        result.put("ethereumUsdt", globalAssetsService.getEthereumUSDT(period));
        result.put("nasdaq", globalAssetsService.getNasdaq(period));
        result.put("bitcoinDominance", globalAssetsService.getBitcoinDominance(period));
        result.put("gold", globalAssetsService.getGoldPrice(period));
        result.put("dollarIndex", globalAssetsService.getDollarIndex(period));
        
        return ResponseEntity.ok(result);
    }
}

