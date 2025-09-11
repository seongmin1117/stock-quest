package com.stockquest.adapter.in.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * WebSocket을 통한 트레이딩 명령 DTO 모음
 */
public class TradingCommand {

    /**
     * 주문 실행 명령
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PlaceOrderCommand {
        
        /**
         * 챌린지 세션 ID
         */
        @NotNull(message = "챌린지 세션 ID는 필수입니다.")
        @Positive(message = "챌린지 세션 ID는 양수여야 합니다.")
        private Long challengeSessionId;
        
        /**
         * 주식 심볼
         */
        @NotBlank(message = "주식 심볼은 필수입니다.")
        @Size(max = 10, message = "주식 심볼은 최대 10자까지 입력 가능합니다.")
        private String symbol;
        
        /**
         * 주문 타입 (BUY, SELL)
         */
        @NotBlank(message = "주문 타입은 필수입니다.")
        @Pattern(regexp = "^(BUY|SELL)$", message = "주문 타입은 BUY 또는 SELL이어야 합니다.")
        private String orderType;
        
        /**
         * 주문 수량
         */
        @NotNull(message = "주문 수량은 필수입니다.")
        @Positive(message = "주문 수량은 양수여야 합니다.")
        @Max(value = 1000000, message = "주문 수량은 최대 1,000,000주까지 가능합니다.")
        private Integer quantity;
        
        /**
         * 주문 가격 (시장가 주문시 null 가능)
         */
        @DecimalMin(value = "0.01", message = "주문 가격은 최소 0.01 이상이어야 합니다.")
        @DecimalMax(value = "999999.99", message = "주문 가격은 최대 999,999.99까지 가능합니다.")
        private BigDecimal price;
        
        /**
         * 주문 방식 (MARKET, LIMIT, STOP)
         */
        @NotBlank(message = "주문 방식은 필수입니다.")
        @Pattern(regexp = "^(MARKET|LIMIT|STOP)$", message = "주문 방식은 MARKET, LIMIT, STOP 중 하나여야 합니다.")
        private String priceType;
        
        /**
         * 주문 유효 기간 (DAY, GTC, IOC, FOK)
         */
        @Builder.Default
        private String timeInForce = "DAY";
        
        /**
         * 클라이언트 주문 ID (추적용)
         */
        private String clientOrderId;
        
        /**
         * 추가 주문 옵션
         */
        private OrderOptions options;
    }

    /**
     * 주문 취소 명령
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CancelOrderCommand {
        
        /**
         * 챌린지 세션 ID
         */
        @NotNull(message = "챌린지 세션 ID는 필수입니다.")
        @Positive(message = "챌린지 세션 ID는 양수여야 합니다.")
        private Long challengeSessionId;
        
        /**
         * 취소할 주문 ID
         */
        @NotNull(message = "주문 ID는 필수입니다.")
        @Positive(message = "주문 ID는 양수여야 합니다.")
        private Long orderId;
        
        /**
         * 클라이언트 주문 ID (추적용)
         */
        private String clientOrderId;
        
        /**
         * 취소 사유 (선택적)
         */
        @Size(max = 200, message = "취소 사유는 최대 200자까지 입력 가능합니다.")
        private String reason;
    }

    /**
     * 포지션 조회 명령
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GetPositionsCommand {
        
        /**
         * 챌린지 세션 ID
         */
        @NotNull(message = "챌린지 세션 ID는 필수입니다.")
        @Positive(message = "챌린지 세션 ID는 양수여야 합니다.")
        private Long challengeSessionId;
        
        /**
         * 특정 심볼만 조회 (선택적)
         */
        private String symbol;
        
        /**
         * 포지션 상태 필터 (OPEN, CLOSED, ALL)
         */
        @Builder.Default
        private String status = "OPEN";
    }

    /**
     * 주문 이력 조회 명령
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GetOrdersCommand {
        
        /**
         * 챌린지 세션 ID
         */
        @NotNull(message = "챌린지 세션 ID는 필수입니다.")
        @Positive(message = "챌린지 세션 ID는 양수여야 합니다.")
        private Long challengeSessionId;
        
        /**
         * 특정 심볼만 조회 (선택적)
         */
        private String symbol;
        
        /**
         * 주문 상태 필터 (PENDING, EXECUTED, CANCELLED, ALL)
         */
        @Builder.Default
        private String status = "ALL";
        
        /**
         * 조회할 주문 개수 (최대 100개)
         */
        @Builder.Default
        @Max(value = 100, message = "최대 100개 주문까지 조회 가능합니다.")
        private Integer limit = 20;
        
        /**
         * 페이지 오프셋
         */
        @Builder.Default
        @PositiveOrZero(message = "오프셋은 0 이상이어야 합니다.")
        private Integer offset = 0;
    }

    /**
     * 포트폴리오 조회 명령
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GetPortfolioCommand {
        
        /**
         * 챌린지 세션 ID
         */
        @NotNull(message = "챌린지 세션 ID는 필수입니다.")
        @Positive(message = "챌린지 세션 ID는 양수여야 합니다.")
        private Long challengeSessionId;
        
        /**
         * 분석 기간 (1D, 1W, 1M, 3M, 6M, 1Y)
         */
        @Builder.Default
        private String timeframe = "1M";
        
        /**
         * 포함할 분석 타입
         */
        private AnalysisOptions analysisOptions;
    }

    /**
     * 주문 추가 옵션
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderOptions {
        
        /**
         * 부분 체결 허용 여부
         */
        @Builder.Default
        private Boolean allowPartialFill = true;
        
        /**
         * 시장 종료 후 주문 허용 여부
         */
        @Builder.Default
        private Boolean allowAfterHours = false;
        
        /**
         * 최대 슬리피지 허용 범위 (퍼센트)
         */
        @DecimalMin(value = "0.0", message = "슬리피지는 0% 이상이어야 합니다.")
        @DecimalMax(value = "10.0", message = "슬리피지는 10% 이하여야 합니다.")
        private BigDecimal maxSlippagePercent;
        
        /**
         * 주문 만료 시간 (Unix timestamp)
         */
        private Long expirationTime;
        
        /**
         * 자동 손절매 가격
         */
        @DecimalMin(value = "0.01", message = "손절매 가격은 최소 0.01 이상이어야 합니다.")
        private BigDecimal stopLossPrice;
        
        /**
         * 자동 익절 가격
         */
        @DecimalMin(value = "0.01", message = "익절 가격은 최소 0.01 이상이어야 합니다.")
        private BigDecimal takeProfitPrice;
    }

    /**
     * 분석 옵션
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AnalysisOptions {
        
        /**
         * 리스크 분석 포함 여부
         */
        @Builder.Default
        private Boolean includeRiskAnalysis = true;
        
        /**
         * 성과 분석 포함 여부
         */
        @Builder.Default
        private Boolean includePerformanceAnalysis = true;
        
        /**
         * 섹터 분석 포함 여부
         */
        @Builder.Default
        private Boolean includeSectorAnalysis = true;
        
        /**
         * AI 추천 포함 여부
         */
        @Builder.Default
        private Boolean includeAIRecommendations = true;
        
        /**
         * 벤치마크 비교 포함 여부
         */
        @Builder.Default
        private Boolean includeBenchmarkComparison = false;
    }
}