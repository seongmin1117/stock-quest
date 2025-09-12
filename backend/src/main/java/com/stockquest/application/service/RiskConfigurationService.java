package com.stockquest.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 리스크 설정 서비스
 * Phase 8.3: Advanced Risk Management - 리스크 한도 및 설정 관리
 */
@Slf4j
@Service
public class RiskConfigurationService {
    
    // 포트폴리오별 리스크 설정 저장소 (실제 구현에서는 데이터베이스)
    private final Map<String, RiskConfiguration> portfolioConfigs = new ConcurrentHashMap<>();
    
    // 시스템 기본 설정
    private final RiskConfiguration defaultConfig = RiskConfiguration.builder()
        .varLimit(new BigDecimal("5.0"))           // 5% VaR 한도
        .maxDrawdownLimit(new BigDecimal("15.0"))   // 15% 최대 낙폭 한도
        .concentrationLimit(new BigDecimal("10.0")) // 10% 집중도 한도
        .leverageLimit(new BigDecimal("3.0"))       // 3배 레버리지 한도
        .minAccuracyThreshold(new BigDecimal("0.80")) // 80% 최소 모델 정확도
        .minLiquidityRatio(new BigDecimal("5.0"))   // 5% 최소 유동성 비율
        .stressTestFailureThreshold(new BigDecimal("20.0")) // 20% 스트레스 테스트 실패 한도
        .build();
    
    /**
     * 포트폴리오의 VaR 한도 조회
     */
    public BigDecimal getVaRLimit(String portfolioId) {
        RiskConfiguration config = getConfiguration(portfolioId);
        log.debug("VaR limit for portfolio {}: {}", portfolioId, config.getVarLimit());
        return config.getVarLimit();
    }
    
    /**
     * 포트폴리오의 최대 낙폭 한도 조회
     */
    public BigDecimal getMaxDrawdownLimit(String portfolioId) {
        RiskConfiguration config = getConfiguration(portfolioId);
        return config.getMaxDrawdownLimit();
    }
    
    /**
     * 포트폴리오의 집중도 한도 조회
     */
    public BigDecimal getConcentrationLimit(String portfolioId) {
        RiskConfiguration config = getConfiguration(portfolioId);
        return config.getConcentrationLimit();
    }
    
    /**
     * 포트폴리오의 레버리지 한도 조회
     */
    public BigDecimal getLeverageLimit(String portfolioId) {
        RiskConfiguration config = getConfiguration(portfolioId);
        return config.getLeverageLimit();
    }
    
    /**
     * 모델 최소 정확도 임계값 조회
     */
    public BigDecimal getMinAccuracyThreshold() {
        return defaultConfig.getMinAccuracyThreshold();
    }
    
    /**
     * 최소 유동성 비율 조회
     */
    public BigDecimal getMinLiquidityRatio(String portfolioId) {
        RiskConfiguration config = getConfiguration(portfolioId);
        return config.getMinLiquidityRatio();
    }
    
    /**
     * 스트레스 테스트 실패 임계값 조회
     */
    public BigDecimal getStressTestFailureThreshold(String portfolioId) {
        RiskConfiguration config = getConfiguration(portfolioId);
        return config.getStressTestFailureThreshold();
    }
    
    /**
     * 포트폴리오 리스크 설정 업데이트
     */
    public void updateRiskConfiguration(String portfolioId, RiskConfiguration configuration) {
        log.info("Updating risk configuration for portfolio: {}", portfolioId);
        
        // 설정 유효성 검증
        validateConfiguration(configuration);
        
        portfolioConfigs.put(portfolioId, configuration);
        
        log.info("Risk configuration updated for portfolio: {} - VaR: {}, MaxDD: {}", 
            portfolioId, configuration.getVarLimit(), configuration.getMaxDrawdownLimit());
    }
    
    /**
     * 포트폴리오별 VaR 한도 설정
     */
    public void setVaRLimit(String portfolioId, BigDecimal varLimit) {
        log.info("Setting VaR limit for portfolio {}: {}", portfolioId, varLimit);
        
        if (varLimit.compareTo(BigDecimal.ZERO) <= 0 || varLimit.compareTo(new BigDecimal("100.0")) > 0) {
            throw new IllegalArgumentException("VaR limit must be between 0 and 100%");
        }
        
        RiskConfiguration config = getConfiguration(portfolioId);
        config.setVarLimit(varLimit);
        portfolioConfigs.put(portfolioId, config);
    }
    
    /**
     * 포트폴리오별 최대 낙폭 한도 설정
     */
    public void setMaxDrawdownLimit(String portfolioId, BigDecimal maxDrawdownLimit) {
        log.info("Setting max drawdown limit for portfolio {}: {}", portfolioId, maxDrawdownLimit);
        
        if (maxDrawdownLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Max drawdown limit must be positive");
        }
        
        RiskConfiguration config = getConfiguration(portfolioId);
        config.setMaxDrawdownLimit(maxDrawdownLimit);
        portfolioConfigs.put(portfolioId, config);
    }
    
    /**
     * 포트폴리오별 집중도 한도 설정
     */
    public void setConcentrationLimit(String portfolioId, BigDecimal concentrationLimit) {
        log.info("Setting concentration limit for portfolio {}: {}", portfolioId, concentrationLimit);
        
        if (concentrationLimit.compareTo(BigDecimal.ZERO) <= 0 || 
            concentrationLimit.compareTo(new BigDecimal("100.0")) > 0) {
            throw new IllegalArgumentException("Concentration limit must be between 0 and 100%");
        }
        
        RiskConfiguration config = getConfiguration(portfolioId);
        config.setConcentrationLimit(concentrationLimit);
        portfolioConfigs.put(portfolioId, config);
    }
    
    /**
     * 포트폴리오별 레버리지 한도 설정
     */
    public void setLeverageLimit(String portfolioId, BigDecimal leverageLimit) {
        log.info("Setting leverage limit for portfolio {}: {}", portfolioId, leverageLimit);
        
        if (leverageLimit.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Leverage limit must be at least 1.0");
        }
        
        RiskConfiguration config = getConfiguration(portfolioId);
        config.setLeverageLimit(leverageLimit);
        portfolioConfigs.put(portfolioId, config);
    }
    
    /**
     * 포트폴리오별 위험 프로파일 설정
     */
    public void setRiskProfile(String portfolioId, RiskProfile riskProfile) {
        log.info("Setting risk profile for portfolio {}: {}", portfolioId, riskProfile);
        
        RiskConfiguration config = getConfiguration(portfolioId);
        
        // 위험 프로파일에 따른 기본 한도 설정
        switch (riskProfile) {
            case CONSERVATIVE -> {
                config.setVarLimit(new BigDecimal("3.0"));
                config.setMaxDrawdownLimit(new BigDecimal("10.0"));
                config.setConcentrationLimit(new BigDecimal("8.0"));
                config.setLeverageLimit(new BigDecimal("2.0"));
            }
            case MODERATE -> {
                config.setVarLimit(new BigDecimal("5.0"));
                config.setMaxDrawdownLimit(new BigDecimal("15.0"));
                config.setConcentrationLimit(new BigDecimal("10.0"));
                config.setLeverageLimit(new BigDecimal("3.0"));
            }
            case AGGRESSIVE -> {
                config.setVarLimit(new BigDecimal("8.0"));
                config.setMaxDrawdownLimit(new BigDecimal("25.0"));
                config.setConcentrationLimit(new BigDecimal("15.0"));
                config.setLeverageLimit(new BigDecimal("5.0"));
            }
            case VERY_AGGRESSIVE -> {
                config.setVarLimit(new BigDecimal("12.0"));
                config.setMaxDrawdownLimit(new BigDecimal("35.0"));
                config.setConcentrationLimit(new BigDecimal("20.0"));
                config.setLeverageLimit(new BigDecimal("10.0"));
            }
        }
        
        config.setRiskProfile(riskProfile);
        portfolioConfigs.put(portfolioId, config);
    }
    
    /**
     * 포트폴리오 리스크 설정 조회
     */
    public RiskConfiguration getRiskConfiguration(String portfolioId) {
        return getConfiguration(portfolioId);
    }
    
    /**
     * 모든 포트폴리오의 리스크 설정 조회
     */
    public Map<String, RiskConfiguration> getAllRiskConfigurations() {
        return new ConcurrentHashMap<>(portfolioConfigs);
    }
    
    /**
     * 포트폴리오 리스크 설정 삭제
     */
    public void deleteRiskConfiguration(String portfolioId) {
        log.info("Deleting risk configuration for portfolio: {}", portfolioId);
        portfolioConfigs.remove(portfolioId);
    }
    
    /**
     * 리스크 한도 위반 여부 확인
     */
    public boolean isVaRLimitBreached(String portfolioId, BigDecimal currentVaR) {
        BigDecimal limit = getVaRLimit(portfolioId);
        boolean breached = currentVaR.compareTo(limit) > 0;
        
        if (breached) {
            log.warn("VaR limit breached for portfolio {}: current={}, limit={}", 
                portfolioId, currentVaR, limit);
        }
        
        return breached;
    }
    
    /**
     * 최대 낙폭 한도 위반 여부 확인
     */
    public boolean isMaxDrawdownLimitBreached(String portfolioId, BigDecimal currentDrawdown) {
        BigDecimal limit = getMaxDrawdownLimit(portfolioId);
        boolean breached = currentDrawdown.abs().compareTo(limit) > 0;
        
        if (breached) {
            log.warn("Max drawdown limit breached for portfolio {}: current={}, limit={}", 
                portfolioId, currentDrawdown.abs(), limit);
        }
        
        return breached;
    }
    
    /**
     * 집중도 한도 위반 여부 확인
     */
    public boolean isConcentrationLimitBreached(String portfolioId, BigDecimal currentConcentration) {
        BigDecimal limit = getConcentrationLimit(portfolioId);
        boolean breached = currentConcentration.compareTo(limit) > 0;
        
        if (breached) {
            log.warn("Concentration limit breached for portfolio {}: current={}, limit={}", 
                portfolioId, currentConcentration, limit);
        }
        
        return breached;
    }
    
    /**
     * 레버리지 한도 위반 여부 확인
     */
    public boolean isLeverageLimitBreached(String portfolioId, BigDecimal currentLeverage) {
        BigDecimal limit = getLeverageLimit(portfolioId);
        boolean breached = currentLeverage.compareTo(limit) > 0;
        
        if (breached) {
            log.warn("Leverage limit breached for portfolio {}: current={}, limit={}", 
                portfolioId, currentLeverage, limit);
        }
        
        return breached;
    }
    
    /**
     * 포트폴리오 리스크 상태 요약
     */
    public RiskStatus getRiskStatus(String portfolioId, RiskMetrics currentMetrics) {
        RiskConfiguration config = getConfiguration(portfolioId);
        
        return RiskStatus.builder()
            .portfolioId(portfolioId)
            .varStatus(createLimitStatus(currentMetrics.getCurrentVaR(), config.getVarLimit()))
            .drawdownStatus(createLimitStatus(currentMetrics.getCurrentDrawdown().abs(), config.getMaxDrawdownLimit()))
            .concentrationStatus(createLimitStatus(currentMetrics.getCurrentConcentration(), config.getConcentrationLimit()))
            .leverageStatus(createLimitStatus(currentMetrics.getCurrentLeverage(), config.getLeverageLimit()))
            .overallStatus(determineOverallStatus(currentMetrics, config))
            .build();
    }
    
    // ========================= 헬퍼 메서드들 =========================
    
    private RiskConfiguration getConfiguration(String portfolioId) {
        return portfolioConfigs.getOrDefault(portfolioId, copyDefaultConfig());
    }
    
    private RiskConfiguration copyDefaultConfig() {
        return RiskConfiguration.builder()
            .varLimit(defaultConfig.getVarLimit())
            .maxDrawdownLimit(defaultConfig.getMaxDrawdownLimit())
            .concentrationLimit(defaultConfig.getConcentrationLimit())
            .leverageLimit(defaultConfig.getLeverageLimit())
            .minAccuracyThreshold(defaultConfig.getMinAccuracyThreshold())
            .minLiquidityRatio(defaultConfig.getMinLiquidityRatio())
            .stressTestFailureThreshold(defaultConfig.getStressTestFailureThreshold())
            .riskProfile(RiskProfile.MODERATE)
            .build();
    }
    
    private void validateConfiguration(RiskConfiguration configuration) {
        if (configuration.getVarLimit() == null || 
            configuration.getVarLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("VaR limit must be positive");
        }
        
        if (configuration.getMaxDrawdownLimit() == null || 
            configuration.getMaxDrawdownLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Max drawdown limit must be positive");
        }
        
        if (configuration.getConcentrationLimit() == null || 
            configuration.getConcentrationLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Concentration limit must be positive");
        }
        
        if (configuration.getLeverageLimit() == null || 
            configuration.getLeverageLimit().compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Leverage limit must be at least 1.0");
        }
    }
    
    private LimitStatus createLimitStatus(BigDecimal currentValue, BigDecimal limit) {
        if (currentValue == null || limit == null) {
            return LimitStatus.UNKNOWN;
        }
        
        BigDecimal utilizationRatio = currentValue.divide(limit, 4, java.math.RoundingMode.HALF_UP);
        
        if (utilizationRatio.compareTo(new BigDecimal("1.0")) > 0) {
            return LimitStatus.BREACHED;
        } else if (utilizationRatio.compareTo(new BigDecimal("0.9")) > 0) {
            return LimitStatus.WARNING;
        } else if (utilizationRatio.compareTo(new BigDecimal("0.7")) > 0) {
            return LimitStatus.CAUTION;
        } else {
            return LimitStatus.NORMAL;
        }
    }
    
    private OverallRiskStatus determineOverallStatus(RiskMetrics metrics, RiskConfiguration config) {
        int breachCount = 0;
        int warningCount = 0;
        
        if (metrics.getCurrentVaR() != null && 
            metrics.getCurrentVaR().compareTo(config.getVarLimit()) > 0) {
            breachCount++;
        } else if (metrics.getCurrentVaR() != null && 
            metrics.getCurrentVaR().divide(config.getVarLimit(), 4, java.math.RoundingMode.HALF_UP)
                .compareTo(new BigDecimal("0.9")) > 0) {
            warningCount++;
        }
        
        if (metrics.getCurrentDrawdown() != null && 
            metrics.getCurrentDrawdown().abs().compareTo(config.getMaxDrawdownLimit()) > 0) {
            breachCount++;
        }
        
        if (metrics.getCurrentConcentration() != null && 
            metrics.getCurrentConcentration().compareTo(config.getConcentrationLimit()) > 0) {
            breachCount++;
        }
        
        if (metrics.getCurrentLeverage() != null && 
            metrics.getCurrentLeverage().compareTo(config.getLeverageLimit()) > 0) {
            breachCount++;
        }
        
        if (breachCount > 0) {
            return OverallRiskStatus.HIGH_RISK;
        } else if (warningCount > 1) {
            return OverallRiskStatus.ELEVATED_RISK;
        } else if (warningCount > 0) {
            return OverallRiskStatus.MODERATE_RISK;
        } else {
            return OverallRiskStatus.LOW_RISK;
        }
    }
    
    // ========================= 데이터 클래스들 =========================
    
    @lombok.Data
    @lombok.Builder
    public static class RiskConfiguration {
        private BigDecimal varLimit;
        private BigDecimal maxDrawdownLimit;
        private BigDecimal concentrationLimit;
        private BigDecimal leverageLimit;
        private BigDecimal minAccuracyThreshold;
        private BigDecimal minLiquidityRatio;
        private BigDecimal stressTestFailureThreshold;
        private RiskProfile riskProfile;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RiskStatus {
        private String portfolioId;
        private LimitStatus varStatus;
        private LimitStatus drawdownStatus;
        private LimitStatus concentrationStatus;
        private LimitStatus leverageStatus;
        private OverallRiskStatus overallStatus;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RiskMetrics {
        private BigDecimal currentVaR;
        private BigDecimal currentDrawdown;
        private BigDecimal currentConcentration;
        private BigDecimal currentLeverage;
    }
    
    public enum RiskProfile {
        CONSERVATIVE("보수적"),
        MODERATE("중도적"),
        AGGRESSIVE("적극적"),
        VERY_AGGRESSIVE("매우 적극적");
        
        private final String description;
        
        RiskProfile(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum LimitStatus {
        NORMAL("정상"),
        CAUTION("주의"),
        WARNING("경고"),
        BREACHED("위반"),
        UNKNOWN("알 수 없음");
        
        private final String description;
        
        LimitStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum OverallRiskStatus {
        LOW_RISK("저위험"),
        MODERATE_RISK("보통위험"),
        ELEVATED_RISK("높은위험"),
        HIGH_RISK("매우높은위험");
        
        private final String description;
        
        OverallRiskStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}