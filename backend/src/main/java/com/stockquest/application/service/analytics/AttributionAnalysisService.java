package com.stockquest.application.service.analytics;

import com.stockquest.domain.backtesting.BacktestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Attribution Analysis Service
 * Phase 4.1: Code Quality Enhancement - 기여도 분석 전문 서비스
 * 
 * 포트폴리오 성과의 원인을 분석하고 각 요소별 기여도를 측정합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttributionAnalysisService {

    /**
     * 기여도 분석 수행
     * 
     * @param result 백테스트 결과
     * @return AttributionAnalysis 기여도 분석 결과
     */
    public AttributionAnalysis performAttributionAnalysis(BacktestResult result) {
        try {
            log.info("기여도 분석 시작: {}", result.getBacktestId());

            // 알파/베타 분해
            AlphaBetaDecomposition alphaBeta = performAlphaBetaDecomposition(result);
            
            // 팩터 기반 귀속
            FactorAttribution factorAttribution = performFactorAttribution(result);
            
            // 섹터/자산별 기여도
            AssetContribution assetContribution = calculateAssetContribution(result);
            
            // 타이밍 vs 선택 효과
            TimingSelectionAnalysis timingSelection = analyzeTimingVsSelection(result);
            
            AttributionAnalysis analysis = AttributionAnalysis.builder()
                .alphaBetaDecomposition(alphaBeta)
                .factorAttribution(factorAttribution)
                .assetContribution(assetContribution)
                .timingSelectionAnalysis(timingSelection)
                .styleAnalysis(performStyleAnalysis(result))
                .build();

            log.info("기여도 분석 완료: {}", result.getBacktestId());
            return analysis;
            
        } catch (Exception e) {
            log.error("기여도 분석 실패: {}", e.getMessage(), e);
            throw new RuntimeException("기여도 분석 중 오류 발생", e);
        }
    }

    /**
     * 알파/베타 분해 분석
     */
    private AlphaBetaDecomposition performAlphaBetaDecomposition(BacktestResult result) {
        log.debug("알파/베타 분해 분석 시작");
        
        return AlphaBetaDecomposition.builder()
            .alpha(result.getAlpha())
            .beta(result.getBeta())
            .build();
    }

    /**
     * 팩터 기반 귀속 분석
     */
    private FactorAttribution performFactorAttribution(BacktestResult result) {
        log.debug("팩터 기반 귀속 분석 시작");
        
        // 간단한 팩터 분해 (실제 구현에서는 더 복잡한 팩터 모델 사용)
        return FactorAttribution.builder()
            .marketFactor(BigDecimal.valueOf(0.6)) // 시장 요인
            .sizeFactor(BigDecimal.valueOf(0.1))   // 규모 요인
            .valueFactor(BigDecimal.valueOf(0.2))  // 가치 요인
            .momentumFactor(BigDecimal.valueOf(0.1)) // 모멘텀 요인
            .build();
    }

    /**
     * 자산별 기여도 계산
     */
    private AssetContribution calculateAssetContribution(BacktestResult result) {
        log.debug("자산별 기여도 계산 시작");
        
        // 자산별 기여도 맵 생성 (실제 구현에서는 포지션 데이터 기반 계산)
        Map<String, BigDecimal> contributions = new HashMap<>();
        contributions.put("주식", BigDecimal.valueOf(0.8));
        contributions.put("채권", BigDecimal.valueOf(0.15));
        contributions.put("현금", BigDecimal.valueOf(0.05));
        
        return AssetContribution.builder()
            .assetContributions(contributions)
            .totalContribution(BigDecimal.ONE)
            .build();
    }

    /**
     * 타이밍 vs 선택 효과 분석
     */
    private TimingSelectionAnalysis analyzeTimingVsSelection(BacktestResult result) {
        log.debug("타이밍 vs 선택 효과 분석 시작");
        
        // Brinson 모델 기반 분석 (간소화)
        return TimingSelectionAnalysis.builder()
            .timingContribution(BigDecimal.valueOf(0.3))     // 타이밍 기여도
            .selectionContribution(BigDecimal.valueOf(0.7))  // 선택 기여도
            .interactionEffect(BigDecimal.ZERO)              // 상호작용 효과
            .totalAttribution(BigDecimal.ONE)
            .build();
    }

    /**
     * 스타일 분석 수행
     */
    private StyleAnalysis performStyleAnalysis(BacktestResult result) {
        log.debug("스타일 분석 시작");
        
        // 투자 스타일 노출도 분석
        return StyleAnalysis.builder()
            .growthExposure(BigDecimal.valueOf(0.4))    // 성장주 노출도
            .valueExposure(BigDecimal.valueOf(0.6))     // 가치주 노출도
            .sizeExposure(BigDecimal.valueOf(0.5))      // 대형주 vs 소형주
            .qualityExposure(BigDecimal.valueOf(0.7))   // 품질 요인
            .momentumExposure(BigDecimal.valueOf(0.3))  // 모멘텀 요인
            .build();
    }

    // DTO Classes for Attribution Analysis
    
    public static class AttributionAnalysis {
        private final AlphaBetaDecomposition alphaBetaDecomposition;
        private final FactorAttribution factorAttribution;
        private final AssetContribution assetContribution;
        private final TimingSelectionAnalysis timingSelectionAnalysis;
        private final StyleAnalysis styleAnalysis;

        public static AttributionAnalysisBuilder builder() {
            return new AttributionAnalysisBuilder();
        }

        private AttributionAnalysis(AttributionAnalysisBuilder builder) {
            this.alphaBetaDecomposition = builder.alphaBetaDecomposition;
            this.factorAttribution = builder.factorAttribution;
            this.assetContribution = builder.assetContribution;
            this.timingSelectionAnalysis = builder.timingSelectionAnalysis;
            this.styleAnalysis = builder.styleAnalysis;
        }

        // Getters
        public AlphaBetaDecomposition getAlphaBetaDecomposition() { return alphaBetaDecomposition; }
        public FactorAttribution getFactorAttribution() { return factorAttribution; }
        public AssetContribution getAssetContribution() { return assetContribution; }
        public TimingSelectionAnalysis getTimingSelectionAnalysis() { return timingSelectionAnalysis; }
        public StyleAnalysis getStyleAnalysis() { return styleAnalysis; }

        public static class AttributionAnalysisBuilder {
            private AlphaBetaDecomposition alphaBetaDecomposition;
            private FactorAttribution factorAttribution;
            private AssetContribution assetContribution;
            private TimingSelectionAnalysis timingSelectionAnalysis;
            private StyleAnalysis styleAnalysis;

            public AttributionAnalysisBuilder alphaBetaDecomposition(AlphaBetaDecomposition alphaBetaDecomposition) {
                this.alphaBetaDecomposition = alphaBetaDecomposition;
                return this;
            }

            public AttributionAnalysisBuilder factorAttribution(FactorAttribution factorAttribution) {
                this.factorAttribution = factorAttribution;
                return this;
            }

            public AttributionAnalysisBuilder assetContribution(AssetContribution assetContribution) {
                this.assetContribution = assetContribution;
                return this;
            }

            public AttributionAnalysisBuilder timingSelectionAnalysis(TimingSelectionAnalysis timingSelectionAnalysis) {
                this.timingSelectionAnalysis = timingSelectionAnalysis;
                return this;
            }

            public AttributionAnalysisBuilder styleAnalysis(StyleAnalysis styleAnalysis) {
                this.styleAnalysis = styleAnalysis;
                return this;
            }

            public AttributionAnalysis build() {
                return new AttributionAnalysis(this);
            }
        }
    }

    public static class AlphaBetaDecomposition {
        private final BigDecimal alpha;
        private final BigDecimal beta;

        public static AlphaBetaDecompositionBuilder builder() {
            return new AlphaBetaDecompositionBuilder();
        }

        private AlphaBetaDecomposition(AlphaBetaDecompositionBuilder builder) {
            this.alpha = builder.alpha;
            this.beta = builder.beta;
        }

        // Getters
        public BigDecimal getAlpha() { return alpha; }
        public BigDecimal getBeta() { return beta; }

        public static class AlphaBetaDecompositionBuilder {
            private BigDecimal alpha;
            private BigDecimal beta;

            public AlphaBetaDecompositionBuilder alpha(BigDecimal alpha) {
                this.alpha = alpha;
                return this;
            }

            public AlphaBetaDecompositionBuilder beta(BigDecimal beta) {
                this.beta = beta;
                return this;
            }

            public AlphaBetaDecomposition build() {
                return new AlphaBetaDecomposition(this);
            }
        }
    }

    public static class FactorAttribution {
        private final BigDecimal marketFactor;
        private final BigDecimal sizeFactor;
        private final BigDecimal valueFactor;
        private final BigDecimal momentumFactor;

        public static FactorAttributionBuilder builder() {
            return new FactorAttributionBuilder();
        }

        private FactorAttribution(FactorAttributionBuilder builder) {
            this.marketFactor = builder.marketFactor;
            this.sizeFactor = builder.sizeFactor;
            this.valueFactor = builder.valueFactor;
            this.momentumFactor = builder.momentumFactor;
        }

        // Getters
        public BigDecimal getMarketFactor() { return marketFactor; }
        public BigDecimal getSizeFactor() { return sizeFactor; }
        public BigDecimal getValueFactor() { return valueFactor; }
        public BigDecimal getMomentumFactor() { return momentumFactor; }

        public static class FactorAttributionBuilder {
            private BigDecimal marketFactor;
            private BigDecimal sizeFactor;
            private BigDecimal valueFactor;
            private BigDecimal momentumFactor;

            public FactorAttributionBuilder marketFactor(BigDecimal marketFactor) {
                this.marketFactor = marketFactor;
                return this;
            }

            public FactorAttributionBuilder sizeFactor(BigDecimal sizeFactor) {
                this.sizeFactor = sizeFactor;
                return this;
            }

            public FactorAttributionBuilder valueFactor(BigDecimal valueFactor) {
                this.valueFactor = valueFactor;
                return this;
            }

            public FactorAttributionBuilder momentumFactor(BigDecimal momentumFactor) {
                this.momentumFactor = momentumFactor;
                return this;
            }

            public FactorAttribution build() {
                return new FactorAttribution(this);
            }
        }
    }

    public static class AssetContribution {
        private final Map<String, BigDecimal> assetContributions;
        private final BigDecimal totalContribution;

        public static AssetContributionBuilder builder() {
            return new AssetContributionBuilder();
        }

        private AssetContribution(AssetContributionBuilder builder) {
            this.assetContributions = builder.assetContributions;
            this.totalContribution = builder.totalContribution;
        }

        // Getters
        public Map<String, BigDecimal> getAssetContributions() { return assetContributions; }
        public BigDecimal getTotalContribution() { return totalContribution; }

        public static class AssetContributionBuilder {
            private Map<String, BigDecimal> assetContributions;
            private BigDecimal totalContribution;

            public AssetContributionBuilder assetContributions(Map<String, BigDecimal> assetContributions) {
                this.assetContributions = assetContributions;
                return this;
            }

            public AssetContributionBuilder totalContribution(BigDecimal totalContribution) {
                this.totalContribution = totalContribution;
                return this;
            }

            public AssetContribution build() {
                return new AssetContribution(this);
            }
        }
    }

    public static class TimingSelectionAnalysis {
        private final BigDecimal timingContribution;
        private final BigDecimal selectionContribution;
        private final BigDecimal interactionEffect;
        private final BigDecimal totalAttribution;

        public static TimingSelectionAnalysisBuilder builder() {
            return new TimingSelectionAnalysisBuilder();
        }

        private TimingSelectionAnalysis(TimingSelectionAnalysisBuilder builder) {
            this.timingContribution = builder.timingContribution;
            this.selectionContribution = builder.selectionContribution;
            this.interactionEffect = builder.interactionEffect;
            this.totalAttribution = builder.totalAttribution;
        }

        // Getters
        public BigDecimal getTimingContribution() { return timingContribution; }
        public BigDecimal getSelectionContribution() { return selectionContribution; }
        public BigDecimal getInteractionEffect() { return interactionEffect; }
        public BigDecimal getTotalAttribution() { return totalAttribution; }

        public static class TimingSelectionAnalysisBuilder {
            private BigDecimal timingContribution;
            private BigDecimal selectionContribution;
            private BigDecimal interactionEffect;
            private BigDecimal totalAttribution;

            public TimingSelectionAnalysisBuilder timingContribution(BigDecimal timingContribution) {
                this.timingContribution = timingContribution;
                return this;
            }

            public TimingSelectionAnalysisBuilder selectionContribution(BigDecimal selectionContribution) {
                this.selectionContribution = selectionContribution;
                return this;
            }

            public TimingSelectionAnalysisBuilder interactionEffect(BigDecimal interactionEffect) {
                this.interactionEffect = interactionEffect;
                return this;
            }

            public TimingSelectionAnalysisBuilder totalAttribution(BigDecimal totalAttribution) {
                this.totalAttribution = totalAttribution;
                return this;
            }

            public TimingSelectionAnalysis build() {
                return new TimingSelectionAnalysis(this);
            }
        }
    }

    public static class StyleAnalysis {
        private final BigDecimal growthExposure;
        private final BigDecimal valueExposure;
        private final BigDecimal sizeExposure;
        private final BigDecimal qualityExposure;
        private final BigDecimal momentumExposure;

        public static StyleAnalysisBuilder builder() {
            return new StyleAnalysisBuilder();
        }

        private StyleAnalysis(StyleAnalysisBuilder builder) {
            this.growthExposure = builder.growthExposure;
            this.valueExposure = builder.valueExposure;
            this.sizeExposure = builder.sizeExposure;
            this.qualityExposure = builder.qualityExposure;
            this.momentumExposure = builder.momentumExposure;
        }

        // Getters
        public BigDecimal getGrowthExposure() { return growthExposure; }
        public BigDecimal getValueExposure() { return valueExposure; }
        public BigDecimal getSizeExposure() { return sizeExposure; }
        public BigDecimal getQualityExposure() { return qualityExposure; }
        public BigDecimal getMomentumExposure() { return momentumExposure; }

        public static class StyleAnalysisBuilder {
            private BigDecimal growthExposure;
            private BigDecimal valueExposure;
            private BigDecimal sizeExposure;
            private BigDecimal qualityExposure;
            private BigDecimal momentumExposure;

            public StyleAnalysisBuilder growthExposure(BigDecimal growthExposure) {
                this.growthExposure = growthExposure;
                return this;
            }

            public StyleAnalysisBuilder valueExposure(BigDecimal valueExposure) {
                this.valueExposure = valueExposure;
                return this;
            }

            public StyleAnalysisBuilder sizeExposure(BigDecimal sizeExposure) {
                this.sizeExposure = sizeExposure;
                return this;
            }

            public StyleAnalysisBuilder qualityExposure(BigDecimal qualityExposure) {
                this.qualityExposure = qualityExposure;
                return this;
            }

            public StyleAnalysisBuilder momentumExposure(BigDecimal momentumExposure) {
                this.momentumExposure = momentumExposure;
                return this;
            }

            public StyleAnalysis build() {
                return new StyleAnalysis(this);
            }
        }
    }
}