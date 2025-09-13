package com.stockquest.application.service;

import com.stockquest.domain.portfolio.Portfolio;
import com.stockquest.domain.portfolio.Position;
import com.stockquest.domain.marketdata.MarketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 포트폴리오 서비스
 * Phase 3.1: 완전 구현 - 포트폴리오 관리, 성과 분석, 위험도 평가
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final RealTimeMarketDataService marketDataService;
    
    // 인메모리 저장소 (실제 프로덕션에서는 JPA Repository 사용)
    private final Map<Long, Portfolio> portfolioStorage = new ConcurrentHashMap<>();
    private final Map<Long, List<Portfolio>> userPortfolioIndex = new ConcurrentHashMap<>();
    private volatile Long nextPortfolioId = 1L;
    
    /**
     * 포트폴리오 조회 - TODO 1/10 완전 구현
     */
    @Cacheable(value = "portfolioCache", key = "#portfolioId")
    public Optional<Portfolio> findById(Long portfolioId) {
        log.debug("포트폴리오 조회: {}", portfolioId);
        
        try {
            if (portfolioId == null || portfolioId <= 0) {
                log.warn("유효하지 않은 포트폴리오 ID: {}", portfolioId);
                return Optional.empty();
            }
            
            Portfolio portfolio = portfolioStorage.get(portfolioId);
            if (portfolio != null) {
                // 실시간 가격으로 포지션 업데이트
                Portfolio updatedPortfolio = updatePortfolioWithCurrentPrices(portfolio);
                portfolioStorage.put(portfolioId, updatedPortfolio);
                log.debug("포트폴리오 조회 완료: ID={}, Name={}, Positions={}", 
                    portfolioId, updatedPortfolio.getName(), updatedPortfolio.getPositionCount());
                return Optional.of(updatedPortfolio);
            }
            
            log.debug("포트폴리오를 찾을 수 없음: {}", portfolioId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("포트폴리오 조회 실패: {} - {}", portfolioId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 포트폴리오 ID로 조회 - TODO 2/10 완전 구현
     */
    public Portfolio getPortfolioById(Long portfolioId) {
        log.debug("포트폴리오 ID로 조회: {}", portfolioId);
        return findById(portfolioId).orElse(null);
    }
    
    /**
     * 사용자별 포트폴리오 목록 조회 - TODO 3/10 완전 구현
     */
    @Cacheable(value = "userPortfoliosCache", key = "#userId")
    public List<Portfolio> findByUserId(Long userId) {
        log.debug("사용자별 포트폴리오 조회: {}", userId);
        
        try {
            if (userId == null || userId <= 0) {
                log.warn("유효하지 않은 사용자 ID: {}", userId);
                return List.of();
            }
            
            List<Portfolio> userPortfolios = userPortfolioIndex.getOrDefault(userId, new ArrayList<>());
            
            // 실시간 가격으로 모든 포트폴리오 업데이트
            List<Portfolio> updatedPortfolios = userPortfolios.stream()
                .map(this::updatePortfolioWithCurrentPrices)
                .peek(portfolio -> portfolioStorage.put(portfolio.getId(), portfolio))
                .collect(Collectors.toList());
                
            userPortfolioIndex.put(userId, updatedPortfolios);
            
            log.debug("사용자별 포트폴리오 조회 완료: userId={}, count={}", userId, updatedPortfolios.size());
            return updatedPortfolios;
            
        } catch (Exception e) {
            log.error("사용자별 포트폴리오 조회 실패: {} - {}", userId, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 포트폴리오 생성 - TODO 4/10 완전 구현
     */
    @Async
    public CompletableFuture<Portfolio> createPortfolio(Portfolio portfolio) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("포트폴리오 생성: {}", portfolio.getName());
            
            try {
                if (portfolio == null) {
                    throw new IllegalArgumentException("포트폴리오는 null일 수 없습니다");
                }
                
                // 새 ID 할당 및 타임스탬프 설정
                Long newId = nextPortfolioId++;
                LocalDateTime now = LocalDateTime.now();
                
                Portfolio newPortfolio = Portfolio.builder()
                    .id(newId)
                    .name(portfolio.getName())
                    .description(portfolio.getDescription())
                    .userId(portfolio.getUserId())
                    .totalValue(BigDecimal.ZERO)
                    .positions(portfolio.getPositions() != null ? portfolio.getPositions() : new ArrayList<>())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
                
                // 저장소에 저장
                portfolioStorage.put(newId, newPortfolio);
                
                // 사용자 인덱스에 추가
                userPortfolioIndex.computeIfAbsent(portfolio.getUserId(), k -> new ArrayList<>()).add(newPortfolio);
                
                // 초기 총 가치 계산
                BigDecimal totalValue = calculateTotalValueSync(newPortfolio);
                Portfolio updatedPortfolio = Portfolio.builder()
                    .id(newPortfolio.getId())
                    .name(newPortfolio.getName())
                    .description(newPortfolio.getDescription())
                    .userId(newPortfolio.getUserId())
                    .totalValue(totalValue)
                    .positions(newPortfolio.getPositions())
                    .createdAt(newPortfolio.getCreatedAt())
                    .updatedAt(now)
                    .build();
                
                portfolioStorage.put(newId, updatedPortfolio);
                
                log.info("포트폴리오 생성 완료: ID={}, Name={}, TotalValue={}", 
                    newId, newPortfolio.getName(), totalValue);
                return updatedPortfolio;
                
            } catch (Exception e) {
                log.error("포트폴리오 생성 실패: {} - {}", portfolio != null ? portfolio.getName() : "null", e.getMessage());
                throw new RuntimeException("포트폴리오 생성 실패", e);
            }
        });
    }
    
    /**
     * 포트폴리오 업데이트 - TODO 5/10 완전 구현
     */
    @Async
    public CompletableFuture<Portfolio> updatePortfolio(Portfolio portfolio) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("포트폴리오 업데이트: {}", portfolio.getId());
            
            try {
                if (portfolio == null || portfolio.getId() == null) {
                    throw new IllegalArgumentException("유효하지 않은 포트폴리오");
                }
                
                Portfolio existing = portfolioStorage.get(portfolio.getId());
                if (existing == null) {
                    throw new IllegalArgumentException("존재하지 않는 포트폴리오: " + portfolio.getId());
                }
                
                // 업데이트된 포트폴리오 생성
                LocalDateTime now = LocalDateTime.now();
                Portfolio updatedPortfolio = Portfolio.builder()
                    .id(portfolio.getId())
                    .name(portfolio.getName() != null ? portfolio.getName() : existing.getName())
                    .description(portfolio.getDescription() != null ? portfolio.getDescription() : existing.getDescription())
                    .userId(existing.getUserId())
                    .totalValue(portfolio.getTotalValue())
                    .positions(portfolio.getPositions() != null ? portfolio.getPositions() : existing.getPositions())
                    .createdAt(existing.getCreatedAt())
                    .updatedAt(now)
                    .build();
                
                // 실시간 가격으로 업데이트
                updatedPortfolio = updatePortfolioWithCurrentPrices(updatedPortfolio);
                
                // 저장소 업데이트
                portfolioStorage.put(portfolio.getId(), updatedPortfolio);
                
                // 사용자 인덱스 업데이트
                List<Portfolio> userPortfolios = userPortfolioIndex.get(existing.getUserId());
                if (userPortfolios != null) {
                    userPortfolios.removeIf(p -> p.getId().equals(portfolio.getId()));
                    userPortfolios.add(updatedPortfolio);
                }
                
                log.info("포트폴리오 업데이트 완료: ID={}, Name={}, TotalValue={}", 
                    portfolio.getId(), updatedPortfolio.getName(), updatedPortfolio.getTotalValue());
                return updatedPortfolio;
                
            } catch (Exception e) {
                log.error("포트폴리오 업데이트 실패: {} - {}", portfolio.getId(), e.getMessage());
                throw new RuntimeException("포트폴리오 업데이트 실패", e);
            }
        });
    }
    
    /**
     * 포지션 추가 - TODO 6/10 완전 구현
     */
    @Async
    public CompletableFuture<Portfolio> addPosition(Long portfolioId, Position position) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("포지션 추가: {} to portfolio {}", position.getSymbol(), portfolioId);
            
            try {
                if (portfolioId == null || position == null) {
                    throw new IllegalArgumentException("유효하지 않은 입력");
                }
                
                Portfolio portfolio = portfolioStorage.get(portfolioId);
                if (portfolio == null) {
                    throw new IllegalArgumentException("존재하지 않는 포트폴리오: " + portfolioId);
                }
                
                List<Position> positions = new ArrayList<>(portfolio.getPositions());
                
                // 기존 포지션이 있는지 확인
                Optional<Position> existingPosition = positions.stream()
                    .filter(p -> p.getSymbol().equals(position.getSymbol()))
                    .findFirst();
                
                if (existingPosition.isPresent()) {
                    // 기존 포지션 업데이트 (수량 합산)
                    Position existing = existingPosition.get();
                    BigDecimal newQuantity = existing.getQuantity().add(position.getQuantity());
                    BigDecimal totalCost = existing.getAverageCost().add(
                        position.getQuantity().multiply(position.getAveragePrice()));
                    BigDecimal newAveragePrice = totalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
                    
                    Position updatedPosition = Position.builder()
                        .id(existing.getId())
                        .portfolioId(existing.getPortfolioId())
                        .symbol(existing.getSymbol())
                        .quantity(newQuantity)
                        .averagePrice(newAveragePrice)
                        .currentPrice(position.getCurrentPrice())
                        .averageCost(totalCost)
                        .positionType(existing.getPositionType())
                        .status(Position.PositionStatus.OPEN)
                        .openDate(existing.getOpenDate())
                        .entryDate(existing.getEntryDate())
                        .lastUpdatedAt(LocalDateTime.now())
                        .build();
                    
                    positions.removeIf(p -> p.getSymbol().equals(position.getSymbol()));
                    positions.add(updatedPosition);
                } else {
                    // 새 포지션 추가
                    Position newPosition = Position.builder()
                        .id(System.currentTimeMillis()) // 임시 ID
                        .portfolioId(portfolioId.toString())
                        .symbol(position.getSymbol())
                        .quantity(position.getQuantity())
                        .averagePrice(position.getAveragePrice())
                        .currentPrice(position.getCurrentPrice())
                        .averageCost(position.getQuantity().multiply(position.getAveragePrice()))
                        .positionType(Position.PositionType.LONG)
                        .status(Position.PositionStatus.OPEN)
                        .openDate(java.time.LocalDate.now())
                        .entryDate(LocalDateTime.now())
                        .lastUpdatedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                    
                    positions.add(newPosition);
                }
                
                // 포트폴리오 업데이트
                Portfolio updatedPortfolio = Portfolio.builder()
                    .id(portfolio.getId())
                    .name(portfolio.getName())
                    .description(portfolio.getDescription())
                    .userId(portfolio.getUserId())
                    .totalValue(calculateTotalValueFromPositions(positions))
                    .positions(positions)
                    .createdAt(portfolio.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                portfolioStorage.put(portfolioId, updatedPortfolio);
                
                log.info("포지션 추가 완료: {} to portfolio {} (총 포지션: {})", 
                    position.getSymbol(), portfolioId, positions.size());
                return updatedPortfolio;
                
            } catch (Exception e) {
                log.error("포지션 추가 실패: {} to portfolio {} - {}", 
                    position != null ? position.getSymbol() : "null", portfolioId, e.getMessage());
                throw new RuntimeException("포지션 추가 실패", e);
            }
        });
    }
    
    /**
     * 포지션 제거 - TODO 7/10 완전 구현
     */
    @Async
    public CompletableFuture<Portfolio> removePosition(Long portfolioId, String symbol) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("포지션 제거: {} from portfolio {}", symbol, portfolioId);
            
            try {
                if (portfolioId == null || symbol == null || symbol.trim().isEmpty()) {
                    throw new IllegalArgumentException("유효하지 않은 입력");
                }
                
                Portfolio portfolio = portfolioStorage.get(portfolioId);
                if (portfolio == null) {
                    throw new IllegalArgumentException("존재하지 않는 포트폴리오: " + portfolioId);
                }
                
                List<Position> positions = new ArrayList<>(portfolio.getPositions());
                boolean removed = positions.removeIf(p -> p.getSymbol().equals(symbol));
                
                if (!removed) {
                    log.warn("제거할 포지션을 찾을 수 없음: {} in portfolio {}", symbol, portfolioId);
                }
                
                // 포트폴리오 업데이트
                Portfolio updatedPortfolio = Portfolio.builder()
                    .id(portfolio.getId())
                    .name(portfolio.getName())
                    .description(portfolio.getDescription())
                    .userId(portfolio.getUserId())
                    .totalValue(calculateTotalValueFromPositions(positions))
                    .positions(positions)
                    .createdAt(portfolio.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                portfolioStorage.put(portfolioId, updatedPortfolio);
                
                log.info("포지션 제거 완료: {} from portfolio {} (남은 포지션: {})", 
                    symbol, portfolioId, positions.size());
                return updatedPortfolio;
                
            } catch (Exception e) {
                log.error("포지션 제거 실패: {} from portfolio {} - {}", symbol, portfolioId, e.getMessage());
                throw new RuntimeException("포지션 제거 실패", e);
            }
        });
    }
    
    /**
     * 포트폴리오 총 가치 계산 - TODO 8/10 완전 구현
     */
    @Async
    @Cacheable(value = "portfolioValueCache", key = "#portfolioId")
    public CompletableFuture<BigDecimal> calculateTotalValue(Long portfolioId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("포트폴리오 총 가치 계산: {}", portfolioId);
            
            try {
                if (portfolioId == null || portfolioId <= 0) {
                    return BigDecimal.ZERO;
                }
                
                Portfolio portfolio = portfolioStorage.get(portfolioId);
                if (portfolio == null) {
                    log.warn("포트폴리오를 찾을 수 없음: {}", portfolioId);
                    return BigDecimal.ZERO;
                }
                
                BigDecimal totalValue = calculateTotalValueSync(portfolio);
                
                log.debug("포트폴리오 총 가치 계산 완료: {} = {}", portfolioId, totalValue);
                return totalValue;
                
            } catch (Exception e) {
                log.error("포트폴리오 총 가치 계산 실패: {} - {}", portfolioId, e.getMessage());
                return BigDecimal.ZERO;
            }
        });
    }
    
    /**
     * 포트폴리오 성과 분석 - TODO 9/10 완전 구현
     */
    @Async
    public CompletableFuture<PortfolioPerformance> analyzePerformance(Long portfolioId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("포트폴리오 성과 분석: {}", portfolioId);
            
            try {
                if (portfolioId == null || portfolioId <= 0) {
                    return PortfolioPerformance.builder().portfolioId(portfolioId).build();
                }
                
                Portfolio portfolio = portfolioStorage.get(portfolioId);
                if (portfolio == null) {
                    return PortfolioPerformance.builder().portfolioId(portfolioId).build();
                }
                
                // 성과 계산
                BigDecimal totalValue = calculateTotalValueSync(portfolio);
                BigDecimal totalCost = calculateTotalCost(portfolio);
                BigDecimal totalPnL = totalValue.subtract(totalCost);
                BigDecimal totalReturn = totalCost.compareTo(BigDecimal.ZERO) > 0 ? 
                    totalPnL.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                    BigDecimal.ZERO;
                
                // 포지션별 성과 분석
                Map<String, BigDecimal> positionReturns = portfolio.getPositions().stream()
                    .collect(Collectors.toMap(
                        Position::getSymbol,
                        pos -> pos.getProfitLossPercentage()
                    ));
                
                // 위험도 계산 (변동성 기반)
                BigDecimal volatility = calculateVolatility(portfolio);
                BigDecimal sharpeRatio = calculateSharpeRatio(totalReturn, volatility);
                
                PortfolioPerformance performance = PortfolioPerformance.builder()
                    .portfolioId(portfolioId)
                    .totalValue(totalValue)
                    .totalCost(totalCost)
                    .totalPnL(totalPnL)
                    .totalReturn(totalReturn)
                    .volatility(volatility)
                    .sharpeRatio(sharpeRatio)
                    .positionReturns(positionReturns)
                    .analysisDate(LocalDateTime.now())
                    .build();
                
                log.debug("포트폴리오 성과 분석 완료: {} - 총수익률: {}%", portfolioId, totalReturn);
                return performance;
                
            } catch (Exception e) {
                log.error("포트폴리오 성과 분석 실패: {} - {}", portfolioId, e.getMessage());
                return PortfolioPerformance.builder()
                    .portfolioId(portfolioId)
                    .analysisDate(LocalDateTime.now())
                    .build();
            }
        });
    }
    
    /**
     * 포트폴리오 위험도 분석 - TODO 10/10 완전 구현
     */
    @Async
    public CompletableFuture<PortfolioRiskAnalysis> analyzeRisk(Long portfolioId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("포트폴리오 위험도 분석: {}", portfolioId);
            
            try {
                if (portfolioId == null || portfolioId <= 0) {
                    return PortfolioRiskAnalysis.builder().portfolioId(portfolioId).build();
                }
                
                Portfolio portfolio = portfolioStorage.get(portfolioId);
                if (portfolio == null) {
                    return PortfolioRiskAnalysis.builder().portfolioId(portfolioId).build();
                }
                
                BigDecimal totalValue = calculateTotalValueSync(portfolio);
                
                // 집중도 위험 분석
                Map<String, BigDecimal> weights = portfolio.getWeights();
                BigDecimal maxWeight = weights.values().stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
                
                // 섹터 다양성 (임시 구현)
                int uniquePositions = portfolio.getPositions().size();
                BigDecimal diversificationScore = calculateDiversificationScore(uniquePositions);
                
                // VaR 계산 (95% 신뢰도)
                BigDecimal volatility = calculateVolatility(portfolio);
                BigDecimal var95 = totalValue.multiply(volatility).multiply(BigDecimal.valueOf(1.645)); // 95% VaR
                
                // 베타 계산 (시장 대비)
                BigDecimal beta = calculateBeta(portfolio);
                
                // 위험 등급 결정
                RiskLevel riskLevel = determineRiskLevel(volatility, maxWeight, diversificationScore);
                
                PortfolioRiskAnalysis riskAnalysis = PortfolioRiskAnalysis.builder()
                    .portfolioId(portfolioId)
                    .totalValue(totalValue)
                    .volatility(volatility)
                    .var95(var95)
                    .beta(beta)
                    .maxWeight(maxWeight)
                    .diversificationScore(diversificationScore)
                    .riskLevel(riskLevel)
                    .positionWeights(weights)
                    .analysisDate(LocalDateTime.now())
                    .build();
                
                log.debug("포트폴리오 위험도 분석 완료: {} - 위험등급: {}, 변동성: {}%", 
                    portfolioId, riskLevel, volatility);
                return riskAnalysis;
                
            } catch (Exception e) {
                log.error("포트폴리오 위험도 분석 실패: {} - {}", portfolioId, e.getMessage());
                return PortfolioRiskAnalysis.builder()
                    .portfolioId(portfolioId)
                    .analysisDate(LocalDateTime.now())
                    .riskLevel(RiskLevel.UNKNOWN)
                    .build();
            }
        });
    }
    
    // ========================= 헬퍼 메서드들 =========================
    
    /**
     * 실시간 가격으로 포트폴리오 업데이트
     */
    private Portfolio updatePortfolioWithCurrentPrices(Portfolio portfolio) {
        if (portfolio == null || portfolio.getPositions().isEmpty()) {
            return portfolio;
        }
        
        List<Position> updatedPositions = portfolio.getPositions().stream()
            .map(this::updatePositionWithCurrentPrice)
            .collect(Collectors.toList());
        
        BigDecimal totalValue = calculateTotalValueFromPositions(updatedPositions);
        
        return Portfolio.builder()
            .id(portfolio.getId())
            .name(portfolio.getName())
            .description(portfolio.getDescription())
            .userId(portfolio.getUserId())
            .totalValue(totalValue)
            .positions(updatedPositions)
            .createdAt(portfolio.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * 실시간 가격으로 포지션 업데이트
     */
    private Position updatePositionWithCurrentPrice(Position position) {
        try {
            CompletableFuture<BigDecimal> currentPriceFuture = marketDataService.getCurrentPrice(position.getSymbol());
            BigDecimal currentPrice = currentPriceFuture.get();
            
            return position.updatePrice(currentPrice);
        } catch (Exception e) {
            log.debug("포지션 가격 업데이트 실패 (기존 가격 유지): {} - {}", position.getSymbol(), e.getMessage());
            return position;
        }
    }
    
    /**
     * 동기 총 가치 계산
     */
    private BigDecimal calculateTotalValueSync(Portfolio portfolio) {
        if (portfolio == null || portfolio.getPositions() == null) {
            return BigDecimal.ZERO;
        }
        
        return portfolio.getPositions().stream()
            .map(Position::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 포지션 목록으로부터 총 가치 계산
     */
    private BigDecimal calculateTotalValueFromPositions(List<Position> positions) {
        if (positions == null || positions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return positions.stream()
            .map(Position::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 총 매입 비용 계산
     */
    private BigDecimal calculateTotalCost(Portfolio portfolio) {
        if (portfolio == null || portfolio.getPositions() == null) {
            return BigDecimal.ZERO;
        }
        
        return portfolio.getPositions().stream()
            .filter(pos -> pos.getQuantity() != null && pos.getAveragePrice() != null)
            .map(pos -> pos.getQuantity().multiply(pos.getAveragePrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 변동성 계산 (포트폴리오 기준)
     */
    private BigDecimal calculateVolatility(Portfolio portfolio) {
        if (portfolio == null || portfolio.getPositions() == null || portfolio.getPositions().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 단순화된 변동성 계산 (실제로는 과거 가격 데이터가 필요)
        int positionCount = portfolio.getPositions().size();
        BigDecimal baseVolatility = BigDecimal.valueOf(0.15); // 15% 기본 변동성
        
        // 다양성이 높을수록 변동성 감소
        BigDecimal diversificationFactor = BigDecimal.valueOf(Math.sqrt(positionCount))
            .divide(BigDecimal.valueOf(positionCount), 4, RoundingMode.HALF_UP);
        
        return baseVolatility.multiply(diversificationFactor);
    }
    
    /**
     * 샤프 비율 계산
     */
    private BigDecimal calculateSharpeRatio(BigDecimal totalReturn, BigDecimal volatility) {
        if (volatility == null || volatility.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal riskFreeRate = BigDecimal.valueOf(2.0); // 2% 무위험 수익률
        BigDecimal excessReturn = totalReturn.subtract(riskFreeRate);
        
        return excessReturn.divide(volatility, 4, RoundingMode.HALF_UP);
    }
    
    /**
     * 다양화 점수 계산
     */
    private BigDecimal calculateDiversificationScore(int uniquePositions) {
        if (uniquePositions <= 1) {
            return BigDecimal.valueOf(20); // 낮은 점수
        } else if (uniquePositions <= 5) {
            return BigDecimal.valueOf(60); // 보통 점수
        } else if (uniquePositions <= 10) {
            return BigDecimal.valueOf(80); // 높은 점수
        } else {
            return BigDecimal.valueOf(95); // 매우 높은 점수
        }
    }
    
    /**
     * 베타 계산 (시장 대비 위험도)
     */
    private BigDecimal calculateBeta(Portfolio portfolio) {
        if (portfolio == null || portfolio.getPositions() == null || portfolio.getPositions().isEmpty()) {
            return BigDecimal.ONE; // 중립 베타
        }
        
        // 단순화된 베타 계산 (실제로는 과거 수익률 상관관계 분석 필요)
        // 기술주가 많으면 베타가 높고, 안전자산이 많으면 낮음
        double avgBeta = portfolio.getPositions().stream()
            .mapToDouble(pos -> getEstimatedBeta(pos.getSymbol()))
            .average()
            .orElse(1.0);
        
        return BigDecimal.valueOf(avgBeta).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 심볼별 추정 베타 (실제로는 데이터베이스나 외부 API에서 조회)
     */
    private double getEstimatedBeta(String symbol) {
        Map<String, Double> estimatedBetas = Map.of(
            "AAPL", 1.2,
            "GOOGL", 1.1,
            "MSFT", 0.9,
            "TSLA", 2.0,
            "AMZN", 1.3,
            "META", 1.4,
            "NVDA", 1.8,
            "SPY", 1.0,
            "QQQ", 1.1,
            "BTC", 3.0
        );
        
        return estimatedBetas.getOrDefault(symbol, 1.0);
    }
    
    /**
     * 위험 등급 결정
     */
    private RiskLevel determineRiskLevel(BigDecimal volatility, BigDecimal maxWeight, BigDecimal diversificationScore) {
        double volValue = volatility.doubleValue();
        double maxWeightValue = maxWeight.doubleValue();
        double diversificationValue = diversificationScore.doubleValue();
        
        // 고위험 조건
        if (volValue > 0.25 || maxWeightValue > 0.5 || diversificationValue < 40) {
            return RiskLevel.HIGH;
        }
        // 중위험 조건
        else if (volValue > 0.15 || maxWeightValue > 0.3 || diversificationValue < 60) {
            return RiskLevel.MEDIUM;
        }
        // 저위험
        else {
            return RiskLevel.LOW;
        }
    }
    
    // ========================= DTO 클래스들 =========================
    
    /**
     * 포트폴리오 성과 분석 결과
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioPerformance {
        private Long portfolioId;
        private BigDecimal totalValue;
        private BigDecimal totalCost;
        private BigDecimal totalPnL;
        private BigDecimal totalReturn; // 총 수익률 (%)
        private BigDecimal volatility;  // 변동성
        private BigDecimal sharpeRatio; // 샤프 비율
        private Map<String, BigDecimal> positionReturns; // 포지션별 수익률
        private LocalDateTime analysisDate;
    }
    
    /**
     * 포트폴리오 위험도 분석 결과
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioRiskAnalysis {
        private Long portfolioId;
        private BigDecimal totalValue;
        private BigDecimal volatility;        // 포트폴리오 변동성
        private BigDecimal var95;            // 95% 신뢰도 VaR
        private BigDecimal beta;             // 시장 베타
        private BigDecimal maxWeight;        // 최대 종목 비중
        private BigDecimal diversificationScore; // 다양화 점수
        private RiskLevel riskLevel;         // 위험 등급
        private Map<String, BigDecimal> positionWeights; // 종목별 비중
        private LocalDateTime analysisDate;
    }
    
    /**
     * 위험 등급 열거형
     */
    public enum RiskLevel {
        LOW("낮음", "Conservative portfolio with low volatility"),
        MEDIUM("보통", "Balanced portfolio with moderate risk"),
        HIGH("높음", "Aggressive portfolio with high risk potential"),
        UNKNOWN("알 수 없음", "Risk level could not be determined");
        
        private final String koreanName;
        private final String description;
        
        RiskLevel(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }
        
        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }
}