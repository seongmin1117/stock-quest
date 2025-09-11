package com.stockquest.domain.challenge;

/**
 * 챌린지 유형
 */
public enum ChallengeType {
    MARKET_CRASH("마켓 크래시", "시장 급락 시나리오에서 손실 최소화 및 회복 전략"),
    BULL_MARKET("상승장", "지속적인 상승장에서 최적의 성장 전략"),
    SECTOR_ROTATION("섹터 로테이션", "섹터별 투자 기회 포착 및 로테이션 전략"),
    VOLATILITY("변동성 거래", "높은 변동성 시장에서의 거래 기법"),
    ESG("ESG 투자", "지속가능한 투자 및 ESG 기준 적용"),
    INTERNATIONAL("해외 시장", "글로벌 시장 투자 기회 및 리스크 관리"),
    OPTIONS("옵션 거래", "옵션 및 파생상품을 활용한 고급 거래 전략"),
    RISK_MANAGEMENT("리스크 관리", "자본 보존 및 리스크 최소화 전략"),
    TOURNAMENT("토너먼트", "경쟁적 투자 챌린지 및 순위 경쟁"),
    EDUCATIONAL("교육용", "투자 학습 및 기초 교육 목적"),
    COMMUNITY("커뮤니티", "사용자 간 협력 및 공유 기반 챌린지");
    
    private final String displayName;
    private final String description;
    
    ChallengeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}