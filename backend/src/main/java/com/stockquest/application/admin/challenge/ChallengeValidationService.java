package com.stockquest.application.admin.challenge;

import com.stockquest.application.admin.challenge.port.in.CreateChallengeUseCase;
import com.stockquest.application.exception.InvalidChallengeStateException;
import com.stockquest.domain.challenge.ChallengeInstrument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 챌린지 유효성 검증 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeValidationService {
    
    /**
     * 투자 상품 유효성 검증
     */
    public void validateInstruments(List<CreateChallengeUseCase.CreateChallengeCommand.ChallengeInstrumentCommand> instruments) {
        if (instruments == null || instruments.isEmpty()) {
            throw new InvalidChallengeStateException("At least one instrument is required");
        }
        
        // 중복 키 검사
        Set<String> keys = new HashSet<>();
        Set<String> tickers = new HashSet<>();
        
        for (CreateChallengeUseCase.CreateChallengeCommand.ChallengeInstrumentCommand instrument : instruments) {
            // 키 중복 검사
            if (!keys.add(instrument.getInstrumentKey())) {
                throw new InvalidChallengeStateException("Duplicate instrument key: " + instrument.getInstrumentKey());
            }
            
            // 티커 중복 검사
            if (!tickers.add(instrument.getActualTicker())) {
                throw new InvalidChallengeStateException("Duplicate ticker: " + instrument.getActualTicker());
            }
            
            // 투자 상품 타입 유효성 검사
            try {
                ChallengeInstrument.InstrumentType.valueOf(instrument.getType());
            } catch (IllegalArgumentException e) {
                throw new InvalidChallengeStateException("Invalid instrument type: " + instrument.getType());
            }
            
            // 키 형식 검증 (A-Z, 0-9만 허용)
            if (!instrument.getInstrumentKey().matches("[A-Z0-9]")) {
                throw new InvalidChallengeStateException("Instrument key must be a single letter or digit: " + instrument.getInstrumentKey());
            }
            
            // 티커 형식 검증 (영문 대문자와 숫자만 허용)
            if (!instrument.getActualTicker().matches("^[A-Z0-9.\\-]{1,10}$")) {
                throw new InvalidChallengeStateException("Invalid ticker format: " + instrument.getActualTicker());
            }
        }
        
        // 최소 하나의 주식 상품 필요
        boolean hasStock = instruments.stream()
                .anyMatch(instrument -> "STOCK".equals(instrument.getType()));
        
        if (!hasStock) {
            throw new InvalidChallengeStateException("At least one stock instrument is required");
        }
        
        // 예금 상품은 최대 1개까지만 허용
        long depositCount = instruments.stream()
                .filter(instrument -> "DEPOSIT".equals(instrument.getType()))
                .count();
        
        if (depositCount > 1) {
            throw new InvalidChallengeStateException("Only one deposit instrument is allowed");
        }
        
        log.info("Instrument validation passed for {} instruments", instruments.size());
    }
    
    /**
     * 챌린지 설정 유효성 검증
     */
    public void validateChallengeSettings(String title, Integer maxParticipants, Integer speedFactor) {
        // 제목 중복 검사 (간단한 예시)
        if (title != null && title.toLowerCase().contains("test")) {
            log.warn("Challenge title contains 'test': {}", title);
        }
        
        // 최대 참여자 수 검증
        if (maxParticipants != null && maxParticipants <= 0) {
            throw new InvalidChallengeStateException("Max participants must be greater than 0");
        }
        
        // 시간 압축 배율 검증
        if (speedFactor != null && (speedFactor < 1 || speedFactor > 100)) {
            throw new InvalidChallengeStateException("Speed factor must be between 1 and 100");
        }
    }
    
    /**
     * 마켓 시나리오 유효성 검증
     */
    public void validateMarketScenario(java.util.Map<String, Object> marketScenario) {
        if (marketScenario == null) {
            return;
        }
        
        // 필수 필드 검증
        if (marketScenario.containsKey("volatility")) {
            Object volatility = marketScenario.get("volatility");
            if (volatility instanceof Number) {
                double vol = ((Number) volatility).doubleValue();
                if (vol < 0 || vol > 1) {
                    throw new InvalidChallengeStateException("Volatility must be between 0 and 1");
                }
            }
        }
        
        // 시장 트렌드 검증
        if (marketScenario.containsKey("trend")) {
            String trend = marketScenario.get("trend").toString();
            if (!Set.of("BULL", "BEAR", "SIDEWAYS", "VOLATILE").contains(trend)) {
                throw new InvalidChallengeStateException("Invalid market trend: " + trend);
            }
        }
    }
    
    /**
     * 성공 기준 유효성 검증
     */
    public void validateSuccessCriteria(java.util.Map<String, Object> successCriteria) {
        if (successCriteria == null) {
            return;
        }
        
        // 목표 수익률 검증
        if (successCriteria.containsKey("targetReturn")) {
            Object targetReturn = successCriteria.get("targetReturn");
            if (targetReturn instanceof Number) {
                double returnRate = ((Number) targetReturn).doubleValue();
                if (returnRate < -0.5 || returnRate > 10.0) { // -50% ~ 1000%
                    throw new InvalidChallengeStateException("Target return must be between -50% and 1000%");
                }
            }
        }
        
        // 최대 손실 한도 검증
        if (successCriteria.containsKey("maxLoss")) {
            Object maxLoss = successCriteria.get("maxLoss");
            if (maxLoss instanceof Number) {
                double lossLimit = ((Number) maxLoss).doubleValue();
                if (lossLimit < 0 || lossLimit > 1) {
                    throw new InvalidChallengeStateException("Max loss must be between 0% and 100%");
                }
            }
        }
    }
}