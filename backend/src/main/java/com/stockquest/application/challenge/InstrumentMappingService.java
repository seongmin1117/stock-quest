package com.stockquest.application.challenge;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeInstrument;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 챌린지 상품 매핑 서비스
 * instrumentKey를 실제 ticker로 변환하는 로직 처리
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InstrumentMappingService {
    
    private final ChallengeRepository challengeRepository;
    
    /**
     * InstrumentKey를 실제 Ticker로 변환
     * 캐시를 사용하여 성능 최적화
     */
    @Cacheable(value = "instrumentMapping", key = "#challengeId + ':' + #instrumentKey")
    public String resolveActualTicker(Long challengeId, String instrumentKey) {
        log.debug("Resolving ticker for challengeId={}, instrumentKey={}", challengeId, instrumentKey);
        
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + challengeId));
        
        Optional<ChallengeInstrument> instrument = challenge.getInstruments().stream()
            .filter(inst -> inst.getInstrumentKey().equals(instrumentKey))
            .findFirst();
        
        if (instrument.isEmpty()) {
            // Fallback: instrumentKey가 실제 ticker인 경우 (개발/테스트용)
            if (isValidTicker(instrumentKey)) {
                log.warn("InstrumentKey가 직접 ticker로 사용됨: {}", instrumentKey);
                return instrumentKey;
            }
            throw new IllegalArgumentException("유효하지 않은 상품 키입니다: " + instrumentKey);
        }
        
        String actualTicker = instrument.get().getActualTicker();
        log.debug("Resolved ticker: {} -> {}", instrumentKey, actualTicker);
        return actualTicker;
    }
    
    /**
     * 실제 Ticker를 InstrumentKey로 역변환 (디버깅용)
     */
    @Cacheable(value = "tickerMapping", key = "#challengeId + ':' + #ticker")
    public String resolveInstrumentKey(Long challengeId, String ticker) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + challengeId));
        
        return challenge.getInstruments().stream()
            .filter(inst -> inst.getActualTicker().equals(ticker))
            .findFirst()
            .map(ChallengeInstrument::getInstrumentKey)
            .orElse(ticker); // Fallback to original ticker
    }
    
    /**
     * 유효한 ticker 형식인지 검증
     */
    private boolean isValidTicker(String ticker) {
        return ticker != null && ticker.matches("^[A-Z]{1,10}$");
    }
    
    /**
     * 챌린지에서 사용 가능한 상품인지 검증
     */
    public boolean isValidInstrumentKey(Long challengeId, String instrumentKey) {
        try {
            resolveActualTicker(challengeId, instrumentKey);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}