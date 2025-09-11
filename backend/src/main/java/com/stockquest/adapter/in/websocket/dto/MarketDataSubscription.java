package com.stockquest.adapter.in.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 실시간 시장 데이터 구독 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketDataSubscription {
    
    /**
     * 구독할 주식 심볼 목록
     */
    @NotEmpty(message = "구독할 심볼이 최소 1개 이상 필요합니다.")
    @Size(max = 20, message = "최대 20개 심볼까지 구독 가능합니다.")
    private List<String> symbols;
    
    /**
     * 데이터 타입
     * QUOTES: 실시간 시세
     * TECHNICAL_INDICATORS: 기술적 지표
     * MARKET_DEPTH: 호가창 데이터
     */
    @NotNull(message = "데이터 타입은 필수입니다.")
    @Pattern(
        regexp = "^(QUOTES|TECHNICAL_INDICATORS|MARKET_DEPTH)$", 
        message = "데이터 타입은 QUOTES, TECHNICAL_INDICATORS, MARKET_DEPTH 중 하나여야 합니다."
    )
    private String dataType;
    
    /**
     * 업데이트 주기 (밀리초)
     * 기본값: 1000ms (1초)
     */
    @Builder.Default
    private Integer updateIntervalMs = 1000;
    
    /**
     * 필터링 조건 (선택적)
     */
    private MarketDataFilter filter;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketDataFilter {
        
        /**
         * 최소 거래량 필터
         */
        private Long minVolume;
        
        /**
         * 최소 가격 변동률 필터 (퍼센트)
         */
        private Double minPriceChangePercent;
        
        /**
         * 최대 가격 변동률 필터 (퍼센트)
         */
        private Double maxPriceChangePercent;
        
        /**
         * 가격 범위 필터 - 최소 가격
         */
        private Double minPrice;
        
        /**
         * 가격 범위 필터 - 최대 가격
         */
        private Double maxPrice;
    }
}