package com.stockquest.adapter.in.web;

import com.stockquest.application.analytics.PortfolioAnalyticsService;
import com.stockquest.adapter.in.web.dto.PortfolioAnalyticsResponse;
import com.stockquest.adapter.in.web.dto.PortfolioRiskAnalysisResponse;
import com.stockquest.adapter.in.web.dto.PortfolioRecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Portfolio Analytics REST Controller
 * 포트폴리오 분석 REST API 컨트롤러
 * 
 * 고급 포트폴리오 분석 기능을 제공합니다:
 * - 종합적인 포트폴리오 분석
 * - 위험 지표 계산
 * - 성과 측정
 * - 자산 배분 분석
 * - AI 기반 추천사항
 */
@Slf4j
@RestController
@RequestMapping("/api/sessions/{sessionId}/analytics")
@RequiredArgsConstructor
@Validated
@Tag(name = "Portfolio Analytics", description = "고급 포트폴리오 분석 API")
public class PortfolioAnalyticsController {

    private final PortfolioAnalyticsService portfolioAnalyticsService;

    /**
     * 포트폴리오 종합 분석 조회
     * 
     * @param sessionId 세션 ID
     * @param timeframe 분석 기간 (1M, 3M, 6M, 1Y, 2Y)
     * @return 종합 분석 결과
     */
    @Operation(
        summary = "포트폴리오 종합 분석", 
        description = "선택된 시간 범위에 대한 포트폴리오의 종합적인 분석 결과를 반환합니다."
    )
    @GetMapping("/comprehensive")
    public ResponseEntity<PortfolioAnalyticsResponse> getComprehensiveAnalytics(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable @NotNull @Positive Long sessionId,
            
            @Parameter(description = "분석 기간", example = "1Y")
            @RequestParam(defaultValue = "1Y") String timeframe) {

        log.info("포트폴리오 종합 분석 요청 - sessionId: {}, timeframe: {}", sessionId, timeframe);

        try {
            PortfolioAnalyticsResponse analytics = portfolioAnalyticsService
                    .calculateComprehensiveAnalytics(sessionId, timeframe);
            
            log.info("포트폴리오 종합 분석 완료 - sessionId: {}, totalValue: {}, returnRate: {}%", 
                    sessionId, analytics.getTotalValue(), analytics.getTotalReturnPercent());
                    
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("포트폴리오 분석 실패 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("포트폴리오 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 위험 분석 조회
     * 
     * @param sessionId 세션 ID
     * @param confidenceLevel VaR 신뢰구간 (기본값: 95)
     * @return 위험 분석 결과
     */
    @Operation(
        summary = "포트폴리오 위험 분석", 
        description = "포트폴리오의 다양한 위험 지표와 분석 결과를 반환합니다."
    )
    @GetMapping("/risk-analysis")
    public ResponseEntity<PortfolioRiskAnalysisResponse> getRiskAnalysis(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable @NotNull @Positive Long sessionId,
            
            @Parameter(description = "VaR 신뢰구간 (%)", example = "95")
            @RequestParam(defaultValue = "95") Double confidenceLevel) {

        log.info("포트폴리오 위험 분석 요청 - sessionId: {}, confidenceLevel: {}%", sessionId, confidenceLevel);

        try {
            PortfolioRiskAnalysisResponse riskAnalysis = portfolioAnalyticsService
                    .calculateRiskAnalysis(sessionId, confidenceLevel);
            
            log.info("포트폴리오 위험 분석 완료 - sessionId: {}, VaR: {}%, 베타: {}", 
                    sessionId, riskAnalysis.getValueAtRisk(), riskAnalysis.getPortfolioBeta());
                    
            return ResponseEntity.ok(riskAnalysis);
            
        } catch (Exception e) {
            log.error("위험 분석 실패 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("위험 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 포트폴리오 추천사항 조회
     * 
     * @param sessionId 세션 ID
     * @param priority 최소 우선순위 필터 (LOW, MEDIUM, HIGH, CRITICAL)
     * @return 추천사항 목록
     */
    @Operation(
        summary = "포트폴리오 추천사항", 
        description = "AI 기반 포트폴리오 개선 추천사항을 반환합니다."
    )
    @GetMapping("/recommendations")
    public ResponseEntity<List<PortfolioRecommendationResponse>> getRecommendations(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable @NotNull @Positive Long sessionId,
            
            @Parameter(description = "최소 우선순위", example = "MEDIUM")
            @RequestParam(defaultValue = "LOW") String priority) {

        log.info("포트폴리오 추천사항 요청 - sessionId: {}, priority: {}", sessionId, priority);

        try {
            List<PortfolioRecommendationResponse> recommendations = portfolioAnalyticsService
                    .generateRecommendations(sessionId, priority);
            
            log.info("포트폴리오 추천사항 생성 완료 - sessionId: {}, 추천사항 수: {}", 
                    sessionId, recommendations.size());
                    
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("추천사항 생성 실패 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("추천사항 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 섹터별 분석 조회
     * 
     * @param sessionId 세션 ID
     * @return 섹터별 분석 결과
     */
    @Operation(
        summary = "섹터별 자산배분 분석", 
        description = "포트폴리오의 섹터별 분석과 배분 현황을 반환합니다."
    )
    @GetMapping("/sector-allocation")
    public ResponseEntity<?> getSectorAllocation(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable @NotNull @Positive Long sessionId) {

        log.info("섹터별 분석 요청 - sessionId: {}", sessionId);

        try {
            var sectorAnalysis = portfolioAnalyticsService.calculateSectorAllocation(sessionId);
            
            log.info("섹터별 분석 완료 - sessionId: {}, 섹터 수: {}", 
                    sessionId, sectorAnalysis.size());
                    
            return ResponseEntity.ok(sectorAnalysis);
            
        } catch (Exception e) {
            log.error("섹터별 분석 실패 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("섹터별 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 성과 벤치마크 비교
     * 
     * @param sessionId 세션 ID
     * @param benchmarkSymbol 벤치마크 심볼 (기본값: KOSPI)
     * @param timeframe 분석 기간
     * @return 벤치마크 비교 결과
     */
    @Operation(
        summary = "성과 벤치마크 비교", 
        description = "선택된 벤치마크와 포트폴리오 성과를 비교 분석합니다."
    )
    @GetMapping("/benchmark-comparison")
    public ResponseEntity<?> getBenchmarkComparison(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable @NotNull @Positive Long sessionId,
            
            @Parameter(description = "벤치마크 심볼", example = "KOSPI")
            @RequestParam(defaultValue = "KOSPI") String benchmarkSymbol,
            
            @Parameter(description = "분석 기간", example = "1Y")
            @RequestParam(defaultValue = "1Y") String timeframe) {

        log.info("벤치마크 비교 분석 요청 - sessionId: {}, benchmark: {}, timeframe: {}", 
                sessionId, benchmarkSymbol, timeframe);

        try {
            var benchmarkComparison = portfolioAnalyticsService
                    .compareToBenchmark(sessionId, benchmarkSymbol, timeframe);
            
            log.info("벤치마크 비교 완료 - sessionId: {}, alpha: {}%, beta: {}", 
                    sessionId, benchmarkComparison.getAlpha(), benchmarkComparison.getBeta());
                    
            return ResponseEntity.ok(benchmarkComparison);
            
        } catch (Exception e) {
            log.error("벤치마크 비교 실패 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("벤치마크 비교 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 상관관계 매트릭스 조회
     * 
     * @param sessionId 세션 ID
     * @return 포지션 간 상관관계 매트릭스
     */
    @Operation(
        summary = "포지션 간 상관관계 분석", 
        description = "보유 종목들 간의 상관관계 매트릭스를 반환합니다."
    )
    @GetMapping("/correlation-matrix")
    public ResponseEntity<?> getCorrelationMatrix(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable @NotNull @Positive Long sessionId) {

        log.info("상관관계 매트릭스 요청 - sessionId: {}", sessionId);

        try {
            var correlationMatrix = portfolioAnalyticsService.calculateCorrelationMatrix(sessionId);
            
            log.info("상관관계 분석 완료 - sessionId: {}, 상관관계 수: {}", 
                    sessionId, correlationMatrix.size());
                    
            return ResponseEntity.ok(correlationMatrix);
            
        } catch (Exception e) {
            log.error("상관관계 분석 실패 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("상관관계 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 포트폴리오 성과 이력 조회
     * 
     * @param sessionId 세션 ID
     * @param timeframe 조회 기간
     * @param interval 데이터 간격 (daily, weekly, monthly)
     * @return 성과 이력 데이터
     */
    @Operation(
        summary = "포트폴리오 성과 이력", 
        description = "지정된 기간의 포트폴리오 성과 이력을 반환합니다."
    )
    @GetMapping("/performance-history")
    public ResponseEntity<?> getPerformanceHistory(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable @NotNull @Positive Long sessionId,
            
            @Parameter(description = "조회 기간", example = "1Y")
            @RequestParam(defaultValue = "1Y") String timeframe,
            
            @Parameter(description = "데이터 간격", example = "daily")
            @RequestParam(defaultValue = "daily") String interval) {

        log.info("성과 이력 요청 - sessionId: {}, timeframe: {}, interval: {}", 
                sessionId, timeframe, interval);

        try {
            var performanceHistory = portfolioAnalyticsService
                    .getPerformanceHistory(sessionId, timeframe, interval);
            
            log.info("성과 이력 조회 완료 - sessionId: {}, 데이터 포인트 수: {}", 
                    sessionId, performanceHistory.size());
                    
            return ResponseEntity.ok(performanceHistory);
            
        } catch (Exception e) {
            log.error("성과 이력 조회 실패 - sessionId: {}, error: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("성과 이력 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}