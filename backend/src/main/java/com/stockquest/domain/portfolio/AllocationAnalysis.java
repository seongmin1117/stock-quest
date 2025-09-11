package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 포트폴리오 자산 배분 분석 도메인 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationAnalysis {
    
    /**
     * 섹터별 배분 비율
     */
    private Map<String, Double> sectorAllocation;
    
    /**
     * 자산 클래스별 배분 비율
     */
    private Map<String, Double> assetClassAllocation;
    
    /**
     * 지역별 배분 비율
     */
    private Map<String, Double> regionAllocation;
    
    /**
     * 시가총액별 배분 비율 (Large, Mid, Small Cap)
     */
    private Map<String, Double> marketCapAllocation;
    
    /**
     * 스타일별 배분 비율 (Growth, Value, Blend)
     */
    private Map<String, Double> styleAllocation;
    
    /**
     * 개별 종목별 비중
     */
    private Map<String, Double> individualWeights;
    
    /**
     * 상위 10개 종목 비중 합계
     */
    private Double top10Concentration;
    
    /**
     * 상위 5개 종목 비중 합계
     */
    private Double top5Concentration;
    
    /**
     * 허핀달 집중도 지수 (HHI)
     */
    private Double herfindahlIndex;
    
    /**
     * 유효 종목 수 (Effective Number of Assets)
     */
    private Double effectiveAssetCount;
    
    /**
     * 최대 개별 종목 비중
     */
    private Double maxIndividualWeight;
    
    /**
     * 최소 개별 종목 비중
     */
    private Double minIndividualWeight;
    
    /**
     * 평균 개별 종목 비중
     */
    private Double averageIndividualWeight;
    
    /**
     * 배분 편차 (표준편차)
     */
    private Double allocationDeviation;
    
    /**
     * 현금 비중
     */
    private Double cashWeight;
    
    /**
     * 배분 다각화 점수 (0-100)
     */
    private Integer diversificationScore;
    
    /**
     * 리밸런싱 필요 여부
     */
    private Boolean needsRebalancing;
    
    /**
     * 권장 리밸런싱 액션
     */
    private Map<String, String> rebalancingActions;
    
    /**
     * 목표 배분과의 차이
     */
    private Map<String, Double> targetDeviations;
    
    /**
     * 배분 변화 추이 (지난 30일)
     */
    private Map<String, Double> allocationTrend;
    
    /**
     * ESG 점수 가중평균
     */
    private Double weightedESGScore;
    
    /**
     * 평균 베타 (가중평균)
     */
    private Double weightedBeta;
    
    /**
     * 평균 P/E 비율 (가중평균)
     */
    private Double weightedPERatio;
    
    /**
     * 평균 배당수익률 (가중평균)
     */
    private Double weightedDividendYield;
    
    /**
     * 계산 기준 시간
     */
    private LocalDateTime calculatedAt;
    
    /**
     * 허핀달 집중도 지수 계산
     */
    public Double calculateHerfindahlIndex() {
        if (individualWeights == null || individualWeights.isEmpty()) {
            return 0.0;
        }
        
        return individualWeights.values().stream()
                .mapToDouble(weight -> weight * weight / 100.0) // weight를 소수로 변환
                .sum();
    }
    
    /**
     * 유효 종목 수 계산
     */
    public Double calculateEffectiveAssetCount() {
        Double hhi = this.herfindahlIndex != null ? this.herfindahlIndex : calculateHerfindahlIndex();
        if (hhi == 0.0) return 0.0;
        
        return 1.0 / hhi;
    }
    
    /**
     * 다각화 점수 계산 (0-100)
     */
    public Integer calculateDiversificationScore() {
        int score = 0;
        
        // 섹터 다각화 점수 (40점)
        if (sectorAllocation != null && sectorAllocation.size() >= 5) {
            double maxSectorWeight = sectorAllocation.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(100.0);
            
            if (maxSectorWeight < 30) score += 40;
            else if (maxSectorWeight < 40) score += 30;
            else if (maxSectorWeight < 50) score += 20;
            else score += 10;
        }
        
        // 개별 종목 집중도 점수 (30점)
        if (top5Concentration != null) {
            if (top5Concentration < 30) score += 30;
            else if (top5Concentration < 50) score += 20;
            else if (top5Concentration < 70) score += 10;
        }
        
        // 자산 클래스 다각화 점수 (20점)
        if (assetClassAllocation != null && assetClassAllocation.size() >= 2) {
            score += 20;
        }
        
        // 지역 다각화 점수 (10점)
        if (regionAllocation != null && regionAllocation.size() >= 2) {
            score += 10;
        }
        
        return Math.min(score, 100);
    }
    
    /**
     * 리밸런싱 필요성 판단
     */
    public Boolean assessRebalancingNeed(Map<String, Double> targetAllocation) {
        if (targetAllocation == null || sectorAllocation == null) {
            return false;
        }
        
        // 목표 배분과 현재 배분의 차이가 5% 이상인 경우 리밸런싱 필요
        return targetAllocation.entrySet().stream()
                .anyMatch(entry -> {
                    String sector = entry.getKey();
                    Double target = entry.getValue();
                    Double current = sectorAllocation.get(sector);
                    
                    if (current == null) current = 0.0;
                    
                    return Math.abs(target - current) > 5.0;
                });
    }
    
    /**
     * 집중도 위험 평가
     */
    public String assessConcentrationRisk() {
        if (top5Concentration == null) return "UNKNOWN";
        
        if (top5Concentration > 70) return "VERY_HIGH";
        if (top5Concentration > 50) return "HIGH";
        if (top5Concentration > 30) return "MEDIUM";
        if (top5Concentration > 15) return "LOW";
        return "VERY_LOW";
    }
    
    /**
     * 배분 효율성 점수 계산
     */
    public Double calculateAllocationEfficiency() {
        Integer divScore = this.diversificationScore != null ? 
                          this.diversificationScore : calculateDiversificationScore();
        
        // 다각화 점수와 집중도 위험을 종합하여 효율성 점수 계산
        double efficiency = divScore / 100.0;
        
        // 집중도 위험 패널티 적용
        String concentrationRisk = assessConcentrationRisk();
        switch (concentrationRisk) {
            case "VERY_HIGH" -> efficiency *= 0.5;
            case "HIGH" -> efficiency *= 0.7;
            case "MEDIUM" -> efficiency *= 0.85;
            case "LOW" -> efficiency *= 0.95;
        }
        
        return efficiency * 100.0;
    }
}