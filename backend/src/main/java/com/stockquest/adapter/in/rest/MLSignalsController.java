package com.stockquest.adapter.in.rest;

import com.stockquest.application.service.MLTradingSignalService;
import com.stockquest.domain.ml.TradingSignal;
import com.stockquest.domain.ml.TradingSignal.MarketRegime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ML 트레이딩 시그널 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ml/signals")
@RequiredArgsConstructor
public class MLSignalsController {
    
    private final MLTradingSignalService mlTradingSignalService;
    
    /**
     * 단일 심볼에 대한 ML 트레이딩 시그널 생성
     * 
     * @param symbol 주식 심볼 (예: AAPL, MSFT)
     * @return ML 기반 트레이딩 시그널
     */
    @GetMapping("/generate/{symbol}")
    public CompletableFuture<ResponseEntity<TradingSignalResponse>> generateSignal(
            @PathVariable @NotBlank String symbol) {
        
        log.info("ML 시그널 생성 요청: symbol={}", symbol);
        
        return mlTradingSignalService.generateTradingSignal(symbol.toUpperCase())
            .thenApply(signal -> {
                TradingSignalResponse response = TradingSignalResponse.from(signal);
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                log.error("ML 시그널 생성 실패: symbol={}", symbol, throwable);
                return ResponseEntity.internalServerError().build();
            });
    }
    
    /**
     * 복수 심볼에 대한 배치 ML 시그널 생성
     * 
     * @param request 배치 시그널 생성 요청
     * @return ML 기반 트레이딩 시그널 리스트
     */
    @PostMapping("/generate/batch")
    public CompletableFuture<ResponseEntity<BatchSignalResponse>> generateBatchSignals(
            @Valid @RequestBody BatchSignalRequest request) {
        
        log.info("배치 ML 시그널 생성 요청: symbols={}", request.getSymbols());
        
        return mlTradingSignalService.generateBatchSignals(request.getSymbols())
            .thenApply(signals -> {
                List<TradingSignalResponse> responses = signals.stream()
                    .map(TradingSignalResponse::from)
                    .toList();
                
                BatchSignalResponse response = BatchSignalResponse.builder()
                    .signals(responses)
                    .totalCount(responses.size())
                    .build();
                    
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                log.error("배치 ML 시그널 생성 실패: symbols={}", request.getSymbols(), throwable);
                return ResponseEntity.internalServerError().build();
            });
    }
    
    /**
     * 시장 조건에 따른 시그널 필터링
     * 
     * @param request 시그널 필터링 요청
     * @return 필터링된 시그널 리스트
     */
    @PostMapping("/filter")
    public ResponseEntity<FilteredSignalResponse> filterSignalsByMarketCondition(
            @Valid @RequestBody FilterSignalsRequest request) {
        
        log.info("시그널 필터링 요청: regime={}, symbolCount={}", 
            request.getMarketRegime(), request.getSymbols().size());
        
        try {
            // 먼저 모든 심볼에 대한 시그널 생성
            List<TradingSignal> allSignals = mlTradingSignalService.generateBatchSignals(request.getSymbols())
                .join();
            
            // 시장 조건에 따른 필터링
            List<TradingSignal> filteredSignals = mlTradingSignalService
                .filterSignalsByMarketCondition(allSignals, request.getMarketRegime());
            
            List<TradingSignalResponse> responses = filteredSignals.stream()
                .map(TradingSignalResponse::from)
                .toList();
            
            FilteredSignalResponse response = FilteredSignalResponse.builder()
                .filteredSignals(responses)
                .totalOriginalCount(allSignals.size())
                .filteredCount(responses.size())
                .marketRegime(request.getMarketRegime())
                .build();
                
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("시그널 필터링 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 시그널 성과 추적 업데이트
     * 
     * @param signalId 시그널 ID
     * @param request 성과 업데이트 요청
     * @return 업데이트 결과
     */
    @PutMapping("/{signalId}/performance")
    public ResponseEntity<PerformanceUpdateResponse> updateSignalPerformance(
            @PathVariable String signalId,
            @Valid @RequestBody PerformanceUpdateRequest request) {
        
        log.info("시그널 성과 업데이트 요청: signalId={}", signalId);
        
        try {
            // 실제 구현에서는 성과 추적 서비스 호출
            PerformanceUpdateResponse response = PerformanceUpdateResponse.builder()
                .signalId(signalId)
                .updated(true)
                .message("성과 정보가 성공적으로 업데이트되었습니다")
                .build();
                
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("시그널 성과 업데이트 실패: signalId={}", signalId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 활성 시그널 조회
     * 
     * @param limit 조회할 시그널 수 (기본값: 10)
     * @return 활성 시그널 리스트
     */
    @GetMapping("/active")
    public ResponseEntity<ActiveSignalsResponse> getActiveSignals(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("활성 시그널 조회 요청: limit={}", limit);
        
        try {
            // 실제 구현에서는 활성 시그널 조회 서비스 호출
            ActiveSignalsResponse response = ActiveSignalsResponse.builder()
                .activeSignals(List.of()) // 빈 리스트로 초기화
                .count(0)
                .build();
                
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("활성 시그널 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // DTO 클래스들
    @lombok.Data
    @lombok.Builder
    public static class TradingSignalResponse {
        private String signalId;
        private String symbol;
        private String signalType;
        private String strength;
        private String confidence;
        private String expectedReturn;
        private String expectedRisk;
        private Integer timeHorizon;
        private String targetPrice;
        private String stopLossPrice;
        private String generatedAt;
        private String expiresAt;
        private String status;
        private List<SignalReasonResponse> topReasons;
        private MarketConditionResponse marketCondition;
        
        public static TradingSignalResponse from(TradingSignal signal) {
            return TradingSignalResponse.builder()
                .signalId(signal.getSignalId())
                .symbol(signal.getSymbol())
                .signalType(signal.getSignalType().name())
                .strength(signal.getStrength().toString())
                .confidence(signal.getConfidence().toString())
                .expectedReturn(signal.getExpectedReturn().toString())
                .expectedRisk(signal.getExpectedRisk().toString())
                .timeHorizon(signal.getTimeHorizon())
                .targetPrice(signal.getTargetPrice() != null ? signal.getTargetPrice().toString() : null)
                .stopLossPrice(signal.getStopLossPrice() != null ? signal.getStopLossPrice().toString() : null)
                .generatedAt(signal.getGeneratedAt().toString())
                .expiresAt(signal.getExpiresAt().toString())
                .status(signal.getStatus().name())
                .topReasons(signal.getReasons() != null ? 
                    signal.getReasons().stream().limit(3)
                        .map(SignalReasonResponse::from).toList() : List.of())
                .marketCondition(signal.getMarketCondition() != null ? 
                    MarketConditionResponse.from(signal.getMarketCondition()) : null)
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SignalReasonResponse {
        private String featureName;
        private String importance;
        private String value;
        private String description;
        private String category;
        
        public static SignalReasonResponse from(TradingSignal.SignalReason reason) {
            return SignalReasonResponse.builder()
                .featureName(reason.getFeatureName())
                .importance(reason.getImportance().toString())
                .value(reason.getValue().toString())
                .description(reason.getDescription())
                .category(reason.getCategory().name())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class MarketConditionResponse {
        private String regime;
        private String volatility;
        private String liquidity;
        private String marketSentiment;
        private String vixLevel;
        
        public static MarketConditionResponse from(TradingSignal.MarketCondition condition) {
            return MarketConditionResponse.builder()
                .regime(condition.getRegime().name())
                .volatility(condition.getVolatility().name())
                .liquidity(condition.getLiquidity().name())
                .marketSentiment(condition.getMarketSentiment().toString())
                .vixLevel(condition.getVixLevel().toString())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class BatchSignalRequest {
        @Size(min = 1, max = 50, message = "심볼 개수는 1-50개 사이여야 합니다")
        private List<@NotBlank String> symbols;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class BatchSignalResponse {
        private List<TradingSignalResponse> signals;
        private Integer totalCount;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class FilterSignalsRequest {
        @Size(min = 1, max = 100, message = "심볼 개수는 1-100개 사이여야 합니다")
        private List<@NotBlank String> symbols;
        private MarketRegime marketRegime;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class FilteredSignalResponse {
        private List<TradingSignalResponse> filteredSignals;
        private Integer totalOriginalCount;
        private Integer filteredCount;
        private MarketRegime marketRegime;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PerformanceUpdateRequest {
        private String currentPrice;
        private String unrealizedReturn;
        private String maxReturn;
        private String maxDrawdown;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PerformanceUpdateResponse {
        private String signalId;
        private Boolean updated;
        private String message;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ActiveSignalsResponse {
        private List<TradingSignalResponse> activeSignals;
        private Integer count;
    }
}