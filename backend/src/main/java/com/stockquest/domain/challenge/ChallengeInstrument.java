package com.stockquest.domain.challenge;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지에 포함된 금융상품
 * 챌린지 진행 중에는 실제 회사명이 숨겨지고 'A, B, C' 등으로 표시됨
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChallengeInstrument {
    
    public enum InstrumentType {
        STOCK("주식", "개별 주식"),
        ETF("ETF", "상장지수펀드"),
        BOND("채권", "국채 또는 회사채"),
        COMMODITY("원자재", "금, 은, 석유 등"),
        CRYPTO("암호화폐", "비트코인, 이더리움 등"),
        INDEX("지수", "S&P500, NASDAQ 등");
        
        private final String displayName;
        private final String description;
        
        InstrumentType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private Long id;
    private Long challengeId;
    private String instrumentKey;  // 챌린지 내 식별키 (A, B, C, ...)
    private String actualTicker;   // 실제 티커 (AAPL, GOOGL, ...)
    private String hiddenName;     // 숨겨진 표시명 (회사 A, 회사 B, ...)
    private String actualName;     // 실제 회사명 (Apple Inc., Alphabet Inc., ...)
    private InstrumentType type;
    
    // 도메인 생성자
    public ChallengeInstrument(Long challengeId, String instrumentKey, String actualTicker, 
                              String hiddenName, String actualName, InstrumentType type) {
        validateInstrumentKey(instrumentKey);
        validateTicker(actualTicker);
        validateNames(hiddenName, actualName);
        
        this.challengeId = challengeId;
        this.instrumentKey = instrumentKey;
        this.actualTicker = actualTicker;
        this.hiddenName = hiddenName;
        this.actualName = actualName;
        this.type = type;
    }
    
    private void validateInstrumentKey(String instrumentKey) {
        if (instrumentKey == null || instrumentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 키는 필수입니다");
        }
        if (!instrumentKey.matches("^[A-Z]$")) {
            throw new IllegalArgumentException("상품 키는 알파벳 한 글자여야 합니다");
        }
    }
    
    private void validateTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("티커는 필수입니다");
        }
        if (!ticker.matches("^[A-Z]{1,10}$")) {
            throw new IllegalArgumentException("유효한 티커 형식이 아닙니다");
        }
    }
    
    private void validateNames(String hiddenName, String actualName) {
        if (hiddenName == null || hiddenName.trim().isEmpty()) {
            throw new IllegalArgumentException("숨겨진 상품명은 필수입니다");
        }
        if (actualName == null || actualName.trim().isEmpty()) {
            throw new IllegalArgumentException("실제 상품명은 필수입니다");
        }
    }
}