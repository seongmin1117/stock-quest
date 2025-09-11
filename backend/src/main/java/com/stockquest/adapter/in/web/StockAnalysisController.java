package com.stockquest.adapter.in.web;

import com.stockquest.application.analysis.ComprehensiveStockAnalysisService;
import com.stockquest.application.analysis.StockAnalysisResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 종합 주식 분석 REST API Controller
 * 실시간 데이터, AI 전략, 기술적/기본적 분석을 통합한 주식 분석 서비스
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stocks/analysis")
@RequiredArgsConstructor
@Validated
@Tag(name = "Stock Analysis", description = "종합 주식 분석 API")
public class StockAnalysisController {

    private final ComprehensiveStockAnalysisService stockAnalysisService;

    /**
     * 단일 주식 종합 분석
     */
    @GetMapping("/{symbol}")
    @Operation(
        summary = "종합 주식 분석", 
        description = "실시간 데이터, AI 전략, 기술적/기본적 분석을 통합한 종합적인 주식 분석을 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "분석 성공",
            content = @Content(schema = @Schema(implementation = StockAnalysisResult.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 심볼"),
        @ApiResponse(responseCode = "500", description = "분석 중 오류 발생")
    })
    public Mono<ResponseEntity<StockAnalysisResult>> analyzeStock(
        @Parameter(description = "주식 심볼 (예: AAPL, TSLA)", required = true)
        @PathVariable @NotBlank String symbol
    ) {
        log.info("🔍 Stock analysis requested for symbol: {}", symbol);
        
        return stockAnalysisService.performComprehensiveAnalysis(symbol.toUpperCase())
            .map(result -> {
                log.info("✅ Analysis completed for {}: Rating = {}", symbol, result.getOverallRating());
                return ResponseEntity.ok(result);
            })
            .doOnError(error -> 
                log.error("❌ Analysis failed for {}: {}", symbol, error.getMessage())
            );
    }

    /**
     * 다중 주식 일괄 분석
     */
    @PostMapping("/bulk")
    @Operation(
        summary = "다중 주식 일괄 분석",
        description = "여러 주식을 동시에 분석하여 결과를 스트리밍으로 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "분석 스트림 시작"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public Flux<StockAnalysisResult> analyzeBulkStocks(
        @Parameter(description = "분석할 주식 심볼 목록", required = true)
        @RequestBody @Size(min = 1, max = 20) List<@NotBlank String> symbols
    ) {
        log.info("📊 Bulk analysis requested for {} symbols: {}", symbols.size(), symbols);
        
        List<String> upperCaseSymbols = symbols.stream()
            .map(String::toUpperCase)
            .distinct()
            .toList();
            
        return stockAnalysisService.performBulkAnalysis(upperCaseSymbols)
            .doOnComplete(() -> 
                log.info("✅ Bulk analysis completed for {} symbols", upperCaseSymbols.size())
            );
    }

    /**
     * 주식 스크리닝 및 필터링
     */
    @GetMapping("/screen")
    @Operation(
        summary = "주식 스크리닝",
        description = "지정된 조건에 따라 주식을 스크리닝하고 필터링합니다."
    )
    public Flux<StockAnalysisResult> screenStocks(
        @Parameter(description = "최소 평가 점수 (1-10)")
        @RequestParam(defaultValue = "6") int minRating,
        
        @Parameter(description = "최대 리스크 레벨 (LOW, MEDIUM, HIGH)")
        @RequestParam(defaultValue = "MEDIUM") String maxRiskLevel,
        
        @Parameter(description = "추천 액션 필터 (BUY, SELL, HOLD)")
        @RequestParam(required = false) String recommendedAction,
        
        @Parameter(description = "최대 결과 개수")
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("🎯 Stock screening requested: minRating={}, maxRisk={}, action={}, limit={}", 
            minRating, maxRiskLevel, recommendedAction, limit);
        
        // 주요 주식 심볼들로 스크리닝 (실제로는 더 큰 주식 DB에서 가져와야 함)
        List<String> majorStocks = List.of(
            "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", 
            "META", "NVDA", "NFLX", "DIS", "BABA",
            "V", "JNJ", "WMT", "PG", "JPM",
            "UNH", "HD", "MA", "BAC", "ADBE"
        );
        
        return stockAnalysisService.performBulkAnalysis(majorStocks)
            .filter(result -> filterByRating(result, minRating))
            .filter(result -> filterByRisk(result, maxRiskLevel))
            .filter(result -> filterByAction(result, recommendedAction))
            .take(limit)
            .doOnComplete(() -> 
                log.info("✅ Stock screening completed with {} results", limit)
            );
    }

    /**
     * 실시간 주식 분석 스트림 (Server-Sent Events)
     */
    @GetMapping(value = "/stream/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "실시간 주식 분석 스트림",
        description = "지정된 주식의 분석 결과를 실시간으로 스트리밍합니다. (30초마다 업데이트)"
    )
    public Flux<StockAnalysisResult> streamStockAnalysis(
        @Parameter(description = "주식 심볼", required = true)
        @PathVariable @NotBlank String symbol
    ) {
        log.info("📡 Real-time analysis stream started for: {}", symbol);
        
        return Flux.interval(Duration.ofSeconds(30)) // 30초마다 업데이트
            .flatMap(tick -> stockAnalysisService.performComprehensiveAnalysis(symbol.toUpperCase()))
            .doOnSubscribe(subscription -> 
                log.info("🔄 Client subscribed to stream for {}", symbol)
            )
            .doOnCancel(() -> 
                log.info("❌ Client unsubscribed from stream for {}", symbol)
            )
            .doOnError(error ->
                log.error("💥 Stream error for {}: {}", symbol, error.getMessage())
            );
    }

    /**
     * 주식 비교 분석
     */
    @PostMapping("/compare")
    @Operation(
        summary = "주식 비교 분석",
        description = "여러 주식을 비교 분석하여 상대적 성과와 투자 우선순위를 제공합니다."
    )
    public Mono<ResponseEntity<StockComparisonResult>> compareStocks(
        @Parameter(description = "비교할 주식 심볼 목록 (2-5개)", required = true)
        @RequestBody @Size(min = 2, max = 5) List<@NotBlank String> symbols
    ) {
        log.info("⚖️ Stock comparison requested for: {}", symbols);
        
        List<String> upperCaseSymbols = symbols.stream()
            .map(String::toUpperCase)
            .distinct()
            .toList();
        
        return stockAnalysisService.performBulkAnalysis(upperCaseSymbols)
            .collectList()
            .map(this::createComparisonResult)
            .map(ResponseEntity::ok)
            .doOnSuccess(result -> 
                log.info("✅ Stock comparison completed for {} symbols", upperCaseSymbols.size())
            );
    }

    // Helper methods for filtering and comparison

    private boolean filterByRating(StockAnalysisResult result, int minRating) {
        return result.getOverallRating() >= minRating;
    }

    private boolean filterByRisk(StockAnalysisResult result, String maxRiskLevel) {
        String resultRisk = result.getRiskAssessment().getRiskLevel();
        
        return switch (maxRiskLevel) {
            case "LOW" -> "LOW".equals(resultRisk);
            case "MEDIUM" -> List.of("LOW", "MEDIUM").contains(resultRisk);
            case "HIGH" -> true; // All risk levels allowed
            default -> true;
        };
    }

    private boolean filterByAction(StockAnalysisResult result, String recommendedAction) {
        if (recommendedAction == null) return true;
        return result.getStrategyAnalysis().getRecommendedAction().equals(recommendedAction);
    }

    private StockComparisonResult createComparisonResult(List<StockAnalysisResult> results) {
        // 비교 결과 생성 로직
        results.sort((a, b) -> b.getOverallRating().compareTo(a.getOverallRating()));
        
        return StockComparisonResult.builder()
            .comparedStocks(results)
            .topPick(results.get(0))
            .averageRating(results.stream()
                .mapToInt(StockAnalysisResult::getOverallRating)
                .average()
                .orElse(0.0))
            .riskDistribution(calculateRiskDistribution(results))
            .comparisonSummary(generateComparisonSummary(results))
            .build();
    }

    private Map<String, Long> calculateRiskDistribution(List<StockAnalysisResult> results) {
        return results.stream()
            .collect(Collectors.groupingBy(
                result -> result.getRiskAssessment().getRiskLevel(),
                Collectors.counting()
            ));
    }

    private String generateComparisonSummary(List<StockAnalysisResult> results) {
        String topStock = results.get(0).getSymbol();
        double avgRating = results.stream()
            .mapToInt(StockAnalysisResult::getOverallRating)
            .average()
            .orElse(0.0);
        
        return String.format(
            "%s가 평균 점수 %.1f로 최고 평가를 받았습니다. 전체 평균 점수: %.1f점",
            topStock, results.get(0).getOverallRating().doubleValue(), avgRating
        );
    }
}

// Additional DTOs for comparison
@Data
@Builder
class StockComparisonResult {
    private List<StockAnalysisResult> comparedStocks;
    private StockAnalysisResult topPick;
    private Double averageRating;
    private Map<String, Long> riskDistribution;
    private String comparisonSummary;
}