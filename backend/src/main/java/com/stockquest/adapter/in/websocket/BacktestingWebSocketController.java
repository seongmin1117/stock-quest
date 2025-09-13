package com.stockquest.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.domain.backtesting.BacktestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 백테스팅 실시간 업데이트 WebSocket 컨트롤러
 * Phase 8.2: Enhanced Trading Intelligence - 실시간 백테스팅 모니터링
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@EnableAsync
public class BacktestingWebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    // 활성화된 백테스트 세션 추적
    private final Map<String, BacktestSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<BacktestResult>> runningBacktests = new ConcurrentHashMap<>();
    
    /**
     * 백테스트 진행상황 구독
     */
    @MessageMapping("/backtesting/subscribe/{backtestId}")
    @SendTo("/topic/backtesting/progress/{backtestId}")
    public BacktestProgressUpdate subscribeToBacktest(@DestinationVariable String backtestId) {
        log.info("백테스트 진행상황 구독: {}", backtestId);
        
        // 기존 세션 정보 반환 또는 새 세션 생성
        BacktestSession session = activeSessions.computeIfAbsent(backtestId, id -> {
            BacktestSession newSession = new BacktestSession();
            newSession.setBacktestId(id);
            newSession.setStartTime(LocalDateTime.now());
            newSession.setStatus("SUBSCRIBED");
            newSession.setProgress(0);
            return newSession;
        });
        
        return BacktestProgressUpdate.builder()
            .backtestId(backtestId)
            .status(session.getStatus())
            .progress(session.getProgress())
            .message("백테스트 진행상황 구독이 시작되었습니다.")
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * 백테스트 시작 알림
     */
    @MessageMapping("/backtesting/start/{backtestId}")
    public void startBacktestNotification(@DestinationVariable String backtestId) {
        log.info("백테스트 시작 알림: {}", backtestId);
        
        BacktestSession session = activeSessions.get(backtestId);
        if (session != null) {
            session.setStatus("RUNNING");
            session.setStartTime(LocalDateTime.now());
            session.setProgress(0);
            
            BacktestProgressUpdate update = BacktestProgressUpdate.builder()
                .backtestId(backtestId)
                .status("RUNNING")
                .progress(0)
                .message("백테스트가 시작되었습니다.")
                .timestamp(LocalDateTime.now())
                .build();
                
            sendProgressUpdate(backtestId, update);
        }
    }
    
    /**
     * 백테스트 완료 알림
     */
    @Async
    public void notifyBacktestComplete(String backtestId, BacktestResult result) {
        log.info("백테스트 완료 알림: {} (수익률: {}%)", backtestId, result.getTotalReturn());
        
        BacktestSession session = activeSessions.get(backtestId);
        if (session != null) {
            session.setStatus("COMPLETED");
            session.setProgress(100);
            session.setCompletionTime(LocalDateTime.now());
            session.setResult(result);
            
            BacktestCompletionUpdate completion = BacktestCompletionUpdate.builder()
                .backtestId(backtestId)
                .status("COMPLETED")
                .progress(100)
                .message("백테스트가 성공적으로 완료되었습니다.")
                .timestamp(LocalDateTime.now())
                .result(result)
                .performanceSummary(createPerformanceSummary(result))
                .build();
            
            sendCompletionUpdate(backtestId, completion);
        }
    }
    
    /**
     * 백테스트 오류 알림
     */
    @Async
    public void notifyBacktestError(String backtestId, String errorMessage) {
        log.error("백테스트 오류 알림: {} - {}", backtestId, errorMessage);
        
        BacktestSession session = activeSessions.get(backtestId);
        if (session != null) {
            session.setStatus("ERROR");
            session.setErrorMessage(errorMessage);
            session.setCompletionTime(LocalDateTime.now());
            
            BacktestProgressUpdate error = BacktestProgressUpdate.builder()
                .backtestId(backtestId)
                .status("ERROR")
                .progress(session.getProgress())
                .message("백테스트 중 오류가 발생했습니다: " + errorMessage)
                .timestamp(LocalDateTime.now())
                .error(errorMessage)
                .build();
            
            sendProgressUpdate(backtestId, error);
        }
    }
    
    /**
     * 백테스트 진행상황 업데이트 (주기적)
     */
    @Scheduled(fixedDelay = 2000) // 2초마다 실행
    public void updateBacktestProgress() {
        for (Map.Entry<String, BacktestSession> entry : activeSessions.entrySet()) {
            String backtestId = entry.getKey();
            BacktestSession session = entry.getValue();
            
            if ("RUNNING".equals(session.getStatus())) {
                // 실제 진행률 계산 (시뮬레이션)
                int newProgress = calculateRealProgress(session);
                
                if (newProgress != session.getProgress() && newProgress < 100) {
                    session.setProgress(newProgress);
                    session.setLastUpdateTime(LocalDateTime.now());
                    
                    BacktestProgressUpdate update = BacktestProgressUpdate.builder()
                        .backtestId(backtestId)
                        .status("RUNNING")
                        .progress(newProgress)
                        .message(String.format("백테스트 진행중... (%d%%)", newProgress))
                        .timestamp(LocalDateTime.now())
                        .estimatedTimeRemaining(calculateEstimatedTime(session))
                        .currentPhase(getCurrentPhase(newProgress))
                        .build();
                    
                    sendProgressUpdate(backtestId, update);
                }
            }
        }
        
        // 완료된 세션 정리 (1시간 후)
        cleanupCompletedSessions();
    }
    
    /**
     * 실시간 거래 신호 업데이트
     */
    @Async
    public void broadcastTradeSignal(String backtestId, BacktestTradeSignal tradeSignal) {
        if (activeSessions.containsKey(backtestId)) {
            String destination = "/topic/backtesting/trades/" + backtestId;
            messagingTemplate.convertAndSend(destination, tradeSignal);
            
            log.debug("거래 신호 전송: {} - {} @ {}", 
                backtestId, tradeSignal.getAction(), tradeSignal.getPrice());
        }
    }
    
    /**
     * 실시간 성과 지표 업데이트
     */
    @Async
    public void broadcastPerformanceUpdate(String backtestId, BacktestPerformanceUpdate performance) {
        if (activeSessions.containsKey(backtestId)) {
            String destination = "/topic/backtesting/performance/" + backtestId;
            messagingTemplate.convertAndSend(destination, performance);
            
            log.debug("성과 지표 전송: {} - 수익률: {}%", 
                backtestId, performance.getCurrentReturn());
        }
    }
    
    // 백테스트 세션 관리 메서드들
    
    public void registerBacktest(String backtestId, CompletableFuture<BacktestResult> future) {
        runningBacktests.put(backtestId, future);
        
        BacktestSession session = activeSessions.computeIfAbsent(backtestId, id -> {
            BacktestSession newSession = new BacktestSession();
            newSession.setBacktestId(id);
            newSession.setStartTime(LocalDateTime.now());
            newSession.setStatus("STARTING");
            newSession.setProgress(0);
            return newSession;
        });
        
        session.setStatus("STARTING");
        
        // 완료 시 자동으로 알림
        future.whenComplete((result, throwable) -> {
            runningBacktests.remove(backtestId);
            if (throwable == null) {
                notifyBacktestComplete(backtestId, result);
            } else {
                notifyBacktestError(backtestId, throwable.getMessage());
            }
        });
    }
    
    public void removeSession(String backtestId) {
        activeSessions.remove(backtestId);
        runningBacktests.remove(backtestId);
        log.info("백테스트 세션 제거: {}", backtestId);
    }
    
    // 헬퍼 메서드들
    
    private void sendProgressUpdate(String backtestId, BacktestProgressUpdate update) {
        String destination = "/topic/backtesting/progress/" + backtestId;
        messagingTemplate.convertAndSend(destination, update);
    }
    
    private void sendCompletionUpdate(String backtestId, BacktestCompletionUpdate completion) {
        String destination = "/topic/backtesting/completed/" + backtestId;
        messagingTemplate.convertAndSend(destination, completion);
    }
    
    private int calculateRealProgress(BacktestSession session) {
        // 실제 구현에서는 백테스트 엔진으로부터 실제 진행률을 가져옴
        long elapsedMinutes = java.time.Duration.between(session.getStartTime(), LocalDateTime.now()).toMinutes();
        
        // 평균 5분 소요 가정
        int progressBasedOnTime = Math.min(95, (int) (elapsedMinutes * 20));
        
        // 현재 진행률에서 점진적 증가 (실제 동작 시뮬레이션)
        return Math.max(session.getProgress(), progressBasedOnTime);
    }
    
    private String calculateEstimatedTime(BacktestSession session) {
        if (session.getProgress() <= 0) return "5-10분";
        
        long elapsedMinutes = java.time.Duration.between(session.getStartTime(), LocalDateTime.now()).toMinutes();
        long estimatedTotal = elapsedMinutes * 100 / session.getProgress();
        long remaining = Math.max(0, estimatedTotal - elapsedMinutes);
        
        if (remaining <= 1) return "1분 미만";
        else if (remaining <= 60) return remaining + "분";
        else return (remaining / 60) + "시간 " + (remaining % 60) + "분";
    }
    
    private String getCurrentPhase(int progress) {
        if (progress < 10) return "초기화 중";
        else if (progress < 30) return "데이터 준비 중";
        else if (progress < 80) return "백테스팅 실행 중";
        else if (progress < 95) return "성과 계산 중";
        else return "마무리 중";
    }
    
    private void cleanupCompletedSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        
        activeSessions.entrySet().removeIf(entry -> {
            BacktestSession session = entry.getValue();
            LocalDateTime endTime = session.getCompletionTime() != null ? 
                session.getCompletionTime() : session.getLastUpdateTime();
            
            return endTime != null && endTime.isBefore(cutoff) && 
                ("COMPLETED".equals(session.getStatus()) || "ERROR".equals(session.getStatus()));
        });
    }
    
    private BacktestPerformanceSummary createPerformanceSummary(BacktestResult result) {
        return BacktestPerformanceSummary.builder()
            .totalReturn(result.getTotalReturn())
            .annualizedReturn(result.getAnnualizedReturn())
            .sharpeRatio(result.getSharpeRatio())
            .maxDrawdown(result.getMaxDrawdown())
            .winRate(result.getWinRate())
            .totalTrades(result.getTotalTrades())
            .profitLossRatio(result.getProfitLossRatio())
            .volatility(result.getVolatility())
            .build();
    }
    
    // DTO 클래스들
    
    /**
     * 백테스트 세션 정보
     */
    public static class BacktestSession {
        private String backtestId;
        private LocalDateTime startTime;
        private LocalDateTime completionTime;
        private LocalDateTime lastUpdateTime;
        private String status;
        private int progress;
        private String errorMessage;
        private BacktestResult result;
        
        // Getters and setters
        public String getBacktestId() { return backtestId; }
        public void setBacktestId(String backtestId) { this.backtestId = backtestId; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getCompletionTime() { return completionTime; }
        public void setCompletionTime(LocalDateTime completionTime) { this.completionTime = completionTime; }
        
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public BacktestResult getResult() { return result; }
        public void setResult(BacktestResult result) { this.result = result; }
    }
    
    /**
     * 백테스트 진행상황 업데이트 DTO
     */
    public static class BacktestProgressUpdate {
        private String backtestId;
        private String status;
        private Integer progress;
        private String message;
        private LocalDateTime timestamp;
        private String estimatedTimeRemaining;
        private String currentPhase;
        private String error;
        
        public static BacktestProgressUpdateBuilder builder() {
            return new BacktestProgressUpdateBuilder();
        }
        
        // Builder pattern implementation
        public static class BacktestProgressUpdateBuilder {
            private String backtestId;
            private String status;
            private Integer progress;
            private String message;
            private LocalDateTime timestamp;
            private String estimatedTimeRemaining;
            private String currentPhase;
            private String error;
            
            public BacktestProgressUpdateBuilder backtestId(String backtestId) { this.backtestId = backtestId; return this; }
            public BacktestProgressUpdateBuilder status(String status) { this.status = status; return this; }
            public BacktestProgressUpdateBuilder progress(Integer progress) { this.progress = progress; return this; }
            public BacktestProgressUpdateBuilder message(String message) { this.message = message; return this; }
            public BacktestProgressUpdateBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public BacktestProgressUpdateBuilder estimatedTimeRemaining(String estimatedTimeRemaining) { this.estimatedTimeRemaining = estimatedTimeRemaining; return this; }
            public BacktestProgressUpdateBuilder currentPhase(String currentPhase) { this.currentPhase = currentPhase; return this; }
            public BacktestProgressUpdateBuilder error(String error) { this.error = error; return this; }
            
            public BacktestProgressUpdate build() {
                BacktestProgressUpdate update = new BacktestProgressUpdate();
                update.backtestId = this.backtestId;
                update.status = this.status;
                update.progress = this.progress;
                update.message = this.message;
                update.timestamp = this.timestamp;
                update.estimatedTimeRemaining = this.estimatedTimeRemaining;
                update.currentPhase = this.currentPhase;
                update.error = this.error;
                return update;
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
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getEstimatedTimeRemaining() { return estimatedTimeRemaining; }
        public void setEstimatedTimeRemaining(String estimatedTimeRemaining) { this.estimatedTimeRemaining = estimatedTimeRemaining; }
        
        public String getCurrentPhase() { return currentPhase; }
        public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
    
    /**
     * 백테스트 완료 업데이트 DTO
     */
    public static class BacktestCompletionUpdate {
        private String backtestId;
        private String status;
        private Integer progress;
        private String message;
        private LocalDateTime timestamp;
        private BacktestResult result;
        private BacktestPerformanceSummary performanceSummary;
        
        public static BacktestCompletionUpdateBuilder builder() {
            return new BacktestCompletionUpdateBuilder();
        }
        
        // Builder pattern implementation
        public static class BacktestCompletionUpdateBuilder {
            private String backtestId;
            private String status;
            private Integer progress;
            private String message;
            private LocalDateTime timestamp;
            private BacktestResult result;
            private BacktestPerformanceSummary performanceSummary;
            
            public BacktestCompletionUpdateBuilder backtestId(String backtestId) { this.backtestId = backtestId; return this; }
            public BacktestCompletionUpdateBuilder status(String status) { this.status = status; return this; }
            public BacktestCompletionUpdateBuilder progress(Integer progress) { this.progress = progress; return this; }
            public BacktestCompletionUpdateBuilder message(String message) { this.message = message; return this; }
            public BacktestCompletionUpdateBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public BacktestCompletionUpdateBuilder result(BacktestResult result) { this.result = result; return this; }
            public BacktestCompletionUpdateBuilder performanceSummary(BacktestPerformanceSummary performanceSummary) { this.performanceSummary = performanceSummary; return this; }
            
            public BacktestCompletionUpdate build() {
                BacktestCompletionUpdate update = new BacktestCompletionUpdate();
                update.backtestId = this.backtestId;
                update.status = this.status;
                update.progress = this.progress;
                update.message = this.message;
                update.timestamp = this.timestamp;
                update.result = this.result;
                update.performanceSummary = this.performanceSummary;
                return update;
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
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public BacktestResult getResult() { return result; }
        public void setResult(BacktestResult result) { this.result = result; }
        
        public BacktestPerformanceSummary getPerformanceSummary() { return performanceSummary; }
        public void setPerformanceSummary(BacktestPerformanceSummary performanceSummary) { this.performanceSummary = performanceSummary; }
    }
    
    /**
     * 백테스트 거래 신호 DTO
     */
    public static class BacktestTradeSignal {
        private String backtestId;
        private String symbol;
        private String action; // BUY, SELL, SELL_SHORT
        private BigDecimal price;
        private BigDecimal quantity;
        private LocalDateTime timestamp;
        private String signal; // ML 신호 타입
        private BigDecimal confidence;
        private String reason;
        
        public static BacktestTradeSignalBuilder builder() {
            return new BacktestTradeSignalBuilder();
        }
        
        // Builder pattern
        public static class BacktestTradeSignalBuilder {
            private String backtestId;
            private String symbol;
            private String action;
            private BigDecimal price;
            private BigDecimal quantity;
            private LocalDateTime timestamp;
            private String signal;
            private BigDecimal confidence;
            private String reason;
            
            public BacktestTradeSignalBuilder backtestId(String backtestId) { this.backtestId = backtestId; return this; }
            public BacktestTradeSignalBuilder symbol(String symbol) { this.symbol = symbol; return this; }
            public BacktestTradeSignalBuilder action(String action) { this.action = action; return this; }
            public BacktestTradeSignalBuilder price(BigDecimal price) { this.price = price; return this; }
            public BacktestTradeSignalBuilder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
            public BacktestTradeSignalBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public BacktestTradeSignalBuilder signal(String signal) { this.signal = signal; return this; }
            public BacktestTradeSignalBuilder confidence(BigDecimal confidence) { this.confidence = confidence; return this; }
            public BacktestTradeSignalBuilder reason(String reason) { this.reason = reason; return this; }
            
            public BacktestTradeSignal build() {
                BacktestTradeSignal tradeSignal = new BacktestTradeSignal();
                tradeSignal.backtestId = this.backtestId;
                tradeSignal.symbol = this.symbol;
                tradeSignal.action = this.action;
                tradeSignal.price = this.price;
                tradeSignal.quantity = this.quantity;
                tradeSignal.timestamp = this.timestamp;
                tradeSignal.signal = this.signal;
                tradeSignal.confidence = this.confidence;
                tradeSignal.reason = this.reason;
                return tradeSignal;
            }
        }
        
        // Getters and setters
        public String getBacktestId() { return backtestId; }
        public void setBacktestId(String backtestId) { this.backtestId = backtestId; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getSignal() { return signal; }
        public void setSignal(String signal) { this.signal = signal; }
        
        public BigDecimal getConfidence() { return confidence; }
        public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    /**
     * 백테스트 성과 업데이트 DTO
     */
    public static class BacktestPerformanceUpdate {
        private String backtestId;
        private BigDecimal currentValue;
        private BigDecimal currentReturn;
        private BigDecimal currentDrawdown;
        private Integer totalTrades;
        private BigDecimal winRate;
        private LocalDateTime timestamp;
        
        public static BacktestPerformanceUpdateBuilder builder() {
            return new BacktestPerformanceUpdateBuilder();
        }
        
        // Builder pattern
        public static class BacktestPerformanceUpdateBuilder {
            private String backtestId;
            private BigDecimal currentValue;
            private BigDecimal currentReturn;
            private BigDecimal currentDrawdown;
            private Integer totalTrades;
            private BigDecimal winRate;
            private LocalDateTime timestamp;
            
            public BacktestPerformanceUpdateBuilder backtestId(String backtestId) { this.backtestId = backtestId; return this; }
            public BacktestPerformanceUpdateBuilder currentValue(BigDecimal currentValue) { this.currentValue = currentValue; return this; }
            public BacktestPerformanceUpdateBuilder currentReturn(BigDecimal currentReturn) { this.currentReturn = currentReturn; return this; }
            public BacktestPerformanceUpdateBuilder currentDrawdown(BigDecimal currentDrawdown) { this.currentDrawdown = currentDrawdown; return this; }
            public BacktestPerformanceUpdateBuilder totalTrades(Integer totalTrades) { this.totalTrades = totalTrades; return this; }
            public BacktestPerformanceUpdateBuilder winRate(BigDecimal winRate) { this.winRate = winRate; return this; }
            public BacktestPerformanceUpdateBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            
            public BacktestPerformanceUpdate build() {
                BacktestPerformanceUpdate update = new BacktestPerformanceUpdate();
                update.backtestId = this.backtestId;
                update.currentValue = this.currentValue;
                update.currentReturn = this.currentReturn;
                update.currentDrawdown = this.currentDrawdown;
                update.totalTrades = this.totalTrades;
                update.winRate = this.winRate;
                update.timestamp = this.timestamp;
                return update;
            }
        }
        
        // Getters and setters
        public String getBacktestId() { return backtestId; }
        public void setBacktestId(String backtestId) { this.backtestId = backtestId; }
        
        public BigDecimal getCurrentValue() { return currentValue; }
        public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }
        
        public BigDecimal getCurrentReturn() { return currentReturn; }
        public void setCurrentReturn(BigDecimal currentReturn) { this.currentReturn = currentReturn; }
        
        public BigDecimal getCurrentDrawdown() { return currentDrawdown; }
        public void setCurrentDrawdown(BigDecimal currentDrawdown) { this.currentDrawdown = currentDrawdown; }
        
        public Integer getTotalTrades() { return totalTrades; }
        public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }
        
        public BigDecimal getWinRate() { return winRate; }
        public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * 백테스트 성과 요약 DTO
     */
    public static class BacktestPerformanceSummary {
        private BigDecimal totalReturn;
        private BigDecimal annualizedReturn;
        private BigDecimal sharpeRatio;
        private BigDecimal maxDrawdown;
        private BigDecimal winRate;
        private Integer totalTrades;
        private BigDecimal profitLossRatio;
        private BigDecimal volatility;
        
        public static BacktestPerformanceSummaryBuilder builder() {
            return new BacktestPerformanceSummaryBuilder();
        }
        
        // Builder pattern
        public static class BacktestPerformanceSummaryBuilder {
            private BigDecimal totalReturn;
            private BigDecimal annualizedReturn;
            private BigDecimal sharpeRatio;
            private BigDecimal maxDrawdown;
            private BigDecimal winRate;
            private Integer totalTrades;
            private BigDecimal profitLossRatio;
            private BigDecimal volatility;
            
            public BacktestPerformanceSummaryBuilder totalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; return this; }
            public BacktestPerformanceSummaryBuilder annualizedReturn(BigDecimal annualizedReturn) { this.annualizedReturn = annualizedReturn; return this; }
            public BacktestPerformanceSummaryBuilder sharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; return this; }
            public BacktestPerformanceSummaryBuilder maxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; return this; }
            public BacktestPerformanceSummaryBuilder winRate(BigDecimal winRate) { this.winRate = winRate; return this; }
            public BacktestPerformanceSummaryBuilder totalTrades(Integer totalTrades) { this.totalTrades = totalTrades; return this; }
            public BacktestPerformanceSummaryBuilder profitLossRatio(BigDecimal profitLossRatio) { this.profitLossRatio = profitLossRatio; return this; }
            public BacktestPerformanceSummaryBuilder volatility(BigDecimal volatility) { this.volatility = volatility; return this; }
            
            public BacktestPerformanceSummary build() {
                BacktestPerformanceSummary summary = new BacktestPerformanceSummary();
                summary.totalReturn = this.totalReturn;
                summary.annualizedReturn = this.annualizedReturn;
                summary.sharpeRatio = this.sharpeRatio;
                summary.maxDrawdown = this.maxDrawdown;
                summary.winRate = this.winRate;
                summary.totalTrades = this.totalTrades;
                summary.profitLossRatio = this.profitLossRatio;
                summary.volatility = this.volatility;
                return summary;
            }
        }
        
        // Getters and setters
        public BigDecimal getTotalReturn() { return totalReturn; }
        public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
        
        public BigDecimal getAnnualizedReturn() { return annualizedReturn; }
        public void setAnnualizedReturn(BigDecimal annualizedReturn) { this.annualizedReturn = annualizedReturn; }
        
        public BigDecimal getSharpeRatio() { return sharpeRatio; }
        public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
        
        public BigDecimal getMaxDrawdown() { return maxDrawdown; }
        public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
        
        public BigDecimal getWinRate() { return winRate; }
        public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
        
        public Integer getTotalTrades() { return totalTrades; }
        public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }
        
        public BigDecimal getProfitLossRatio() { return profitLossRatio; }
        public void setProfitLossRatio(BigDecimal profitLossRatio) { this.profitLossRatio = profitLossRatio; }
        
        public BigDecimal getVolatility() { return volatility; }
        public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }
    }
}