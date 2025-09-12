package com.stockquest.adapter.web;

import com.stockquest.application.service.BacktestingService;
import com.stockquest.domain.backtesting.BacktestParameters;
import com.stockquest.domain.backtesting.BacktestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * 백테스팅 REST API 컨트롤러
 * Phase 8.2: Enhanced Trading Intelligence - 백테스팅 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/backtesting")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BacktestingController {
    
    private final BacktestingService backtestingService;
    
    // 실행 중인 백테스트 추적
    private final Map<String, CompletableFuture<BacktestResult>> runningBacktests = new ConcurrentHashMap<>();
    private final Map<String, BacktestResult> completedBacktests = new ConcurrentHashMap<>();
    
    /**
     * 새로운 백테스트 시작
     */
    @PostMapping("/run")
    public ResponseEntity<BacktestResponse> runBacktest(@Valid @RequestBody BacktestRequest request) {
        try {
            log.info("백테스트 요청 수신: {}", request.getBacktestName());
            
            // 요청을 BacktestParameters로 변환
            BacktestParameters parameters = convertToParameters(request);
            
            // 백테스트 실행
            CompletableFuture<BacktestResult> future = backtestingService.runBacktest(parameters);
            String backtestId = parameters.getBacktestName() + "_" + System.currentTimeMillis();
            
            // 실행 중인 백테스트로 추가
            runningBacktests.put(backtestId, future);
            
            // 완료 시 결과 저장
            future.whenComplete((result, throwable) -> {
                runningBacktests.remove(backtestId);
                if (throwable == null) {
                    completedBacktests.put(backtestId, result);
                    log.info("백테스트 완료: {} (총수익률: {}%)", 
                        backtestId, result.getTotalReturn());
                } else {
                    log.error("백테스트 실패: {}", backtestId, throwable);
                }
            });
            
            BacktestResponse response = BacktestResponse.builder()
                .backtestId(backtestId)
                .status("RUNNING")
                .message("백테스트가 시작되었습니다.")
                .startTime(LocalDateTime.now())
                .estimatedDuration("5-10분")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("백테스트 시작 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(BacktestResponse.builder()
                    .status("ERROR")
                    .message("백테스트 시작 중 오류 발생: " + e.getMessage())
                    .build());
        }
    }
    
    /**
     * 백테스트 상태 조회
     */
    @GetMapping("/status/{backtestId}")
    public ResponseEntity<BacktestStatusResponse> getBacktestStatus(@PathVariable String backtestId) {
        try {
            CompletableFuture<BacktestResult> runningBacktest = runningBacktests.get(backtestId);
            BacktestResult completedResult = completedBacktests.get(backtestId);
            
            BacktestStatusResponse.BacktestStatusResponseBuilder responseBuilder = BacktestStatusResponse.builder()
                .backtestId(backtestId);
            
            if (runningBacktest != null) {
                // 실행 중
                responseBuilder
                    .status("RUNNING")
                    .progress(calculateProgress(runningBacktest))
                    .message("백테스트 실행 중입니다.");
            } else if (completedResult != null) {
                // 완료됨
                responseBuilder
                    .status("COMPLETED")
                    .progress(100)
                    .message("백테스트가 성공적으로 완료되었습니다.")
                    .result(completedResult);
            } else {
                // 찾을 수 없음
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(responseBuilder.build());
            
        } catch (Exception e) {
            log.error("백테스트 상태 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(BacktestStatusResponse.builder()
                    .backtestId(backtestId)
                    .status("ERROR")
                    .message("상태 조회 중 오류 발생: " + e.getMessage())
                    .build());
        }
    }
    
    /**
     * 백테스트 결과 조회
     */
    @GetMapping("/results/{backtestId}")
    public ResponseEntity<BacktestResult> getBacktestResult(@PathVariable String backtestId) {
        try {
            BacktestResult result = completedBacktests.get(backtestId);
            
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                CompletableFuture<BacktestResult> runningBacktest = runningBacktests.get(backtestId);
                if (runningBacktest != null) {
                    return ResponseEntity.accepted().build(); // 아직 실행 중
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
            
        } catch (Exception e) {
            log.error("백테스트 결과 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 모든 백테스트 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<BacktestSummary>> listBacktests() {
        try {
            List<BacktestSummary> summaries = completedBacktests.entrySet().stream()
                .map(entry -> {
                    String id = entry.getKey();
                    BacktestResult result = entry.getValue();
                    
                    return BacktestSummary.builder()
                        .backtestId(id)
                        .backtestName(result.getBacktestId())
                        .symbol(result.getSymbol())
                        .startDate(result.getStartDate())
                        .endDate(result.getEndDate())
                        .totalReturn(result.getTotalReturn())
                        .sharpeRatio(result.getSharpeRatio())
                        .maxDrawdown(result.getMaxDrawdown())
                        .totalTrades(result.getTotalTrades())
                        .winRate(result.getWinRate())
                        .executionTime(result.getExecutionTime())
                        .build();
                })
                .toList();
                
            return ResponseEntity.ok(summaries);
            
        } catch (Exception e) {
            log.error("백테스트 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 실행 중인 백테스트 취소
     */
    @DeleteMapping("/cancel/{backtestId}")
    public ResponseEntity<Map<String, String>> cancelBacktest(@PathVariable String backtestId) {
        try {
            CompletableFuture<BacktestResult> runningBacktest = runningBacktests.get(backtestId);
            
            if (runningBacktest != null) {
                runningBacktest.cancel(true);
                runningBacktests.remove(backtestId);
                
                log.info("백테스트 취소: {}", backtestId);
                return ResponseEntity.ok(Map.of("message", "백테스트가 취소되었습니다."));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("백테스트 취소 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "백테스트 취소 중 오류 발생: " + e.getMessage()));
        }
    }
    
    /**
     * 백테스트 결과 삭제
     */
    @DeleteMapping("/results/{backtestId}")
    public ResponseEntity<Map<String, String>> deleteBacktestResult(@PathVariable String backtestId) {
        try {
            BacktestResult removed = completedBacktests.remove(backtestId);
            
            if (removed != null) {
                log.info("백테스트 결과 삭제: {}", backtestId);
                return ResponseEntity.ok(Map.of("message", "백테스트 결과가 삭제되었습니다."));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("백테스트 결과 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "결과 삭제 중 오류 발생: " + e.getMessage()));
        }
    }
    
    /**
     * 백테스트 성과 비교
     */
    @PostMapping("/compare")
    public ResponseEntity<BacktestComparisonResult> compareBacktests(@RequestBody BacktestComparisonRequest request) {
        try {
            List<BacktestResult> results = request.getBacktestIds().stream()
                .map(completedBacktests::get)
                .filter(result -> result != null)
                .toList();
            
            if (results.size() < 2) {
                return ResponseEntity.badRequest().build();
            }
            
            BacktestComparisonResult comparison = performComparison(results);
            return ResponseEntity.ok(comparison);
            
        } catch (Exception e) {
            log.error("백테스트 비교 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // DTO 클래스들
    
    /**
     * 백테스트 요청 DTO
     */
    public static class BacktestRequest {
        private String backtestName;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<String> symbols;
        private BigDecimal initialCapital;
        private String benchmarkSymbol;
        private TradingStrategyRequest strategy;
        private MLModelConfigRequest mlConfig;
        
        // Getters and setters
        public String getBacktestName() { return backtestName; }
        public void setBacktestName(String backtestName) { this.backtestName = backtestName; }
        
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        
        public List<String> getSymbols() { return symbols; }
        public void setSymbols(List<String> symbols) { this.symbols = symbols; }
        
        public BigDecimal getInitialCapital() { return initialCapital; }
        public void setInitialCapital(BigDecimal initialCapital) { this.initialCapital = initialCapital; }
        
        public String getBenchmarkSymbol() { return benchmarkSymbol; }
        public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
        
        public TradingStrategyRequest getStrategy() { return strategy; }
        public void setStrategy(TradingStrategyRequest strategy) { this.strategy = strategy; }
        
        public MLModelConfigRequest getMlConfig() { return mlConfig; }
        public void setMlConfig(MLModelConfigRequest mlConfig) { this.mlConfig = mlConfig; }
        
        public static class TradingStrategyRequest {
            private String strategyType = "ML_SIGNALS";
            private BigDecimal signalThreshold = BigDecimal.valueOf(0.6);
            private BigDecimal stopLossPercent = BigDecimal.valueOf(5.0);
            private BigDecimal takeProfitPercent = BigDecimal.valueOf(10.0);
            private Integer maxHoldingPeriod = 30;
            private Boolean allowShortSelling = false;
            private BigDecimal maxPositionSize = BigDecimal.valueOf(10.0);
            private Integer maxConcurrentPositions = 5;
            
            // Getters and setters
            public String getStrategyType() { return strategyType; }
            public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
            
            public BigDecimal getSignalThreshold() { return signalThreshold; }
            public void setSignalThreshold(BigDecimal signalThreshold) { this.signalThreshold = signalThreshold; }
            
            public BigDecimal getStopLossPercent() { return stopLossPercent; }
            public void setStopLossPercent(BigDecimal stopLossPercent) { this.stopLossPercent = stopLossPercent; }
            
            public BigDecimal getTakeProfitPercent() { return takeProfitPercent; }
            public void setTakeProfitPercent(BigDecimal takeProfitPercent) { this.takeProfitPercent = takeProfitPercent; }
            
            public Integer getMaxHoldingPeriod() { return maxHoldingPeriod; }
            public void setMaxHoldingPeriod(Integer maxHoldingPeriod) { this.maxHoldingPeriod = maxHoldingPeriod; }
            
            public Boolean getAllowShortSelling() { return allowShortSelling; }
            public void setAllowShortSelling(Boolean allowShortSelling) { this.allowShortSelling = allowShortSelling; }
            
            public BigDecimal getMaxPositionSize() { return maxPositionSize; }
            public void setMaxPositionSize(BigDecimal maxPositionSize) { this.maxPositionSize = maxPositionSize; }
            
            public Integer getMaxConcurrentPositions() { return maxConcurrentPositions; }
            public void setMaxConcurrentPositions(Integer maxConcurrentPositions) { this.maxConcurrentPositions = maxConcurrentPositions; }
        }
        
        public static class MLModelConfigRequest {
            private String modelType = "SimpleTradingModel";
            private Integer trainingPeriod = 252;
            private BigDecimal confidenceThreshold = BigDecimal.valueOf(0.6);
            
            // Getters and setters
            public String getModelType() { return modelType; }
            public void setModelType(String modelType) { this.modelType = modelType; }
            
            public Integer getTrainingPeriod() { return trainingPeriod; }
            public void setTrainingPeriod(Integer trainingPeriod) { this.trainingPeriod = trainingPeriod; }
            
            public BigDecimal getConfidenceThreshold() { return confidenceThreshold; }
            public void setConfidenceThreshold(BigDecimal confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
        }
    }
    
    /**
     * 백테스트 응답 DTO
     */
    public static class BacktestResponse {
        private String backtestId;
        private String status;
        private String message;
        private LocalDateTime startTime;
        private String estimatedDuration;
        
        public static BacktestResponseBuilder builder() {
            return new BacktestResponseBuilder();
        }
        
        // Builder pattern
        public static class BacktestResponseBuilder {
            private String backtestId;
            private String status;
            private String message;
            private LocalDateTime startTime;
            private String estimatedDuration;
            
            public BacktestResponseBuilder backtestId(String backtestId) { this.backtestId = backtestId; return this; }
            public BacktestResponseBuilder status(String status) { this.status = status; return this; }
            public BacktestResponseBuilder message(String message) { this.message = message; return this; }
            public BacktestResponseBuilder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
            public BacktestResponseBuilder estimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; return this; }
            
            public BacktestResponse build() {
                BacktestResponse response = new BacktestResponse();
                response.backtestId = this.backtestId;
                response.status = this.status;
                response.message = this.message;
                response.startTime = this.startTime;
                response.estimatedDuration = this.estimatedDuration;
                return response;
            }
        }
        
        // Getters and setters
        public String getBacktestId() { return backtestId; }
        public void setBacktestId(String backtestId) { this.backtestId = backtestId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public String getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }
    }
    
    /**
     * 백테스트 상태 응답 DTO
     */
    public static class BacktestStatusResponse {
        private String backtestId;
        private String status;
        private Integer progress;
        private String message;
        private BacktestResult result;
        
        public static BacktestStatusResponseBuilder builder() {
            return new BacktestStatusResponseBuilder();
        }
        
        // Builder pattern
        public static class BacktestStatusResponseBuilder {
            private String backtestId;
            private String status;
            private Integer progress;
            private String message;
            private BacktestResult result;
            
            public BacktestStatusResponseBuilder backtestId(String backtestId) { this.backtestId = backtestId; return this; }
            public BacktestStatusResponseBuilder status(String status) { this.status = status; return this; }
            public BacktestStatusResponseBuilder progress(Integer progress) { this.progress = progress; return this; }
            public BacktestStatusResponseBuilder message(String message) { this.message = message; return this; }
            public BacktestStatusResponseBuilder result(BacktestResult result) { this.result = result; return this; }
            
            public BacktestStatusResponse build() {
                BacktestStatusResponse response = new BacktestStatusResponse();
                response.backtestId = this.backtestId;
                response.status = this.status;
                response.progress = this.progress;
                response.message = this.message;
                response.result = this.result;
                return response;
            }
        }
        
        // Getters and setters
        public String getBacktestId() { return backtestId; }
        public void setBacktestId(String backtestId) { this.backtestId = backtestId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public BacktestResult getResult() { return result; }
        public void setResult(BacktestResult result) { this.result = result; }
    }
    
    /**
     * 백테스트 요약 DTO
     */
    public static class BacktestSummary {
        private String backtestId;
        private String backtestName;
        private String symbol;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal totalReturn;
        private BigDecimal sharpeRatio;
        private BigDecimal maxDrawdown;
        private Integer totalTrades;
        private BigDecimal winRate;
        private LocalDateTime executionTime;
        
        public static BacktestSummaryBuilder builder() {
            return new BacktestSummaryBuilder();
        }
        
        // Builder pattern
        public static class BacktestSummaryBuilder {
            private String backtestId;
            private String backtestName;
            private String symbol;
            private LocalDateTime startDate;
            private LocalDateTime endDate;
            private BigDecimal totalReturn;
            private BigDecimal sharpeRatio;
            private BigDecimal maxDrawdown;
            private Integer totalTrades;
            private BigDecimal winRate;
            private LocalDateTime executionTime;
            
            public BacktestSummaryBuilder backtestId(String backtestId) { this.backtestId = backtestId; return this; }
            public BacktestSummaryBuilder backtestName(String backtestName) { this.backtestName = backtestName; return this; }
            public BacktestSummaryBuilder symbol(String symbol) { this.symbol = symbol; return this; }
            public BacktestSummaryBuilder startDate(LocalDateTime startDate) { this.startDate = startDate; return this; }
            public BacktestSummaryBuilder endDate(LocalDateTime endDate) { this.endDate = endDate; return this; }
            public BacktestSummaryBuilder totalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; return this; }
            public BacktestSummaryBuilder sharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; return this; }
            public BacktestSummaryBuilder maxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; return this; }
            public BacktestSummaryBuilder totalTrades(Integer totalTrades) { this.totalTrades = totalTrades; return this; }
            public BacktestSummaryBuilder winRate(BigDecimal winRate) { this.winRate = winRate; return this; }
            public BacktestSummaryBuilder executionTime(LocalDateTime executionTime) { this.executionTime = executionTime; return this; }
            
            public BacktestSummary build() {
                BacktestSummary summary = new BacktestSummary();
                summary.backtestId = this.backtestId;
                summary.backtestName = this.backtestName;
                summary.symbol = this.symbol;
                summary.startDate = this.startDate;
                summary.endDate = this.endDate;
                summary.totalReturn = this.totalReturn;
                summary.sharpeRatio = this.sharpeRatio;
                summary.maxDrawdown = this.maxDrawdown;
                summary.totalTrades = this.totalTrades;
                summary.winRate = this.winRate;
                summary.executionTime = this.executionTime;
                return summary;
            }
        }
        
        // Getters and setters
        public String getBacktestId() { return backtestId; }
        public void setBacktestId(String backtestId) { this.backtestId = backtestId; }
        
        public String getBacktestName() { return backtestName; }
        public void setBacktestName(String backtestName) { this.backtestName = backtestName; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        
        public BigDecimal getTotalReturn() { return totalReturn; }
        public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
        
        public BigDecimal getSharpeRatio() { return sharpeRatio; }
        public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
        
        public BigDecimal getMaxDrawdown() { return maxDrawdown; }
        public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
        
        public Integer getTotalTrades() { return totalTrades; }
        public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }
        
        public BigDecimal getWinRate() { return winRate; }
        public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
        
        public LocalDateTime getExecutionTime() { return executionTime; }
        public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }
    }
    
    /**
     * 백테스트 비교 요청 DTO
     */
    public static class BacktestComparisonRequest {
        private List<String> backtestIds;
        
        public List<String> getBacktestIds() { return backtestIds; }
        public void setBacktestIds(List<String> backtestIds) { this.backtestIds = backtestIds; }
    }
    
    /**
     * 백테스트 비교 결과 DTO
     */
    public static class BacktestComparisonResult {
        private List<String> backtestIds;
        private Map<String, BigDecimal> totalReturns;
        private Map<String, BigDecimal> sharpeRatios;
        private Map<String, BigDecimal> maxDrawdowns;
        private Map<String, BigDecimal> winRates;
        private String bestPerformer;
        private String recommendation;
        
        // Getters and setters
        public List<String> getBacktestIds() { return backtestIds; }
        public void setBacktestIds(List<String> backtestIds) { this.backtestIds = backtestIds; }
        
        public Map<String, BigDecimal> getTotalReturns() { return totalReturns; }
        public void setTotalReturns(Map<String, BigDecimal> totalReturns) { this.totalReturns = totalReturns; }
        
        public Map<String, BigDecimal> getSharpeRatios() { return sharpeRatios; }
        public void setSharpeRatios(Map<String, BigDecimal> sharpeRatios) { this.sharpeRatios = sharpeRatios; }
        
        public Map<String, BigDecimal> getMaxDrawdowns() { return maxDrawdowns; }
        public void setMaxDrawdowns(Map<String, BigDecimal> maxDrawdowns) { this.maxDrawdowns = maxDrawdowns; }
        
        public Map<String, BigDecimal> getWinRates() { return winRates; }
        public void setWinRates(Map<String, BigDecimal> winRates) { this.winRates = winRates; }
        
        public String getBestPerformer() { return bestPerformer; }
        public void setBestPerformer(String bestPerformer) { this.bestPerformer = bestPerformer; }
        
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }
    
    // 헬퍼 메서드들
    
    private BacktestParameters convertToParameters(BacktestRequest request) {
        BacktestParameters.TradingStrategy strategy = BacktestParameters.TradingStrategy.builder()
            .strategyType(request.getStrategy().getStrategyType())
            .signalThreshold(request.getStrategy().getSignalThreshold())
            .stopLossPercent(request.getStrategy().getStopLossPercent())
            .takeProfitPercent(request.getStrategy().getTakeProfitPercent())
            .maxHoldingPeriod(request.getStrategy().getMaxHoldingPeriod())
            .allowShortSelling(request.getStrategy().getAllowShortSelling())
            .maxPositionSize(request.getStrategy().getMaxPositionSize())
            .maxConcurrentPositions(request.getStrategy().getMaxConcurrentPositions())
            .build();
        
        BacktestParameters.MLModelConfig mlConfig = BacktestParameters.MLModelConfig.builder()
            .modelType(request.getMlConfig().getModelType())
            .trainingPeriod(request.getMlConfig().getTrainingPeriod())
            .confidenceThreshold(request.getMlConfig().getConfidenceThreshold())
            .build();
        
        return BacktestParameters.builder()
            .backtestName(request.getBacktestName())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .symbols(request.getSymbols())
            .initialCapital(request.getInitialCapital())
            .benchmarkSymbol(request.getBenchmarkSymbol())
            .strategy(strategy)
            .mlConfig(mlConfig)
            .build();
    }
    
    private Integer calculateProgress(CompletableFuture<BacktestResult> future) {
        // 실제 구현에서는 더 정확한 진행률 계산이 필요
        if (future.isDone()) {
            return 100;
        } else {
            // 단순한 시간 기반 추정
            return Math.min(90, (int) (Math.random() * 80 + 10));
        }
    }
    
    private BacktestComparisonResult performComparison(List<BacktestResult> results) {
        BacktestComparisonResult comparison = new BacktestComparisonResult();
        
        comparison.setBacktestIds(results.stream()
            .map(BacktestResult::getBacktestId)
            .toList());
        
        comparison.setTotalReturns(results.stream()
            .collect(ConcurrentHashMap::new, 
                (map, result) -> map.put(result.getBacktestId(), result.getTotalReturn()),
                ConcurrentHashMap::putAll));
        
        comparison.setSharpeRatios(results.stream()
            .collect(ConcurrentHashMap::new,
                (map, result) -> map.put(result.getBacktestId(), result.getSharpeRatio()),
                ConcurrentHashMap::putAll));
        
        comparison.setMaxDrawdowns(results.stream()
            .collect(ConcurrentHashMap::new,
                (map, result) -> map.put(result.getBacktestId(), result.getMaxDrawdown()),
                ConcurrentHashMap::putAll));
        
        comparison.setWinRates(results.stream()
            .collect(ConcurrentHashMap::new,
                (map, result) -> map.put(result.getBacktestId(), result.getWinRate()),
                ConcurrentHashMap::putAll));
        
        // 최고 성과자 결정 (샤프 비율 기준)
        BacktestResult bestResult = results.stream()
            .max((r1, r2) -> r1.getSharpeRatio().compareTo(r2.getSharpeRatio()))
            .orElse(results.get(0));
        
        comparison.setBestPerformer(bestResult.getBacktestId());
        comparison.setRecommendation(String.format(
            "샤프 비율이 가장 높은 %s 전략을 권장합니다. (샤프 비율: %.2f, 수익률: %.2f%%)",
            bestResult.getBacktestId(),
            bestResult.getSharpeRatio(),
            bestResult.getTotalReturn()
        ));
        
        return comparison;
    }
}