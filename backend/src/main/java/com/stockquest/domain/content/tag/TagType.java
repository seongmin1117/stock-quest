package com.stockquest.domain.content.tag;

/**
 * 태그 타입 분류
 * 투자 도메인별 태그 카테고리
 */
public enum TagType {

    /**
     * 주식 관련 태그
     */
    STOCK("주식", "주식 투자 관련 태그", "#2196F3"),

    /**
     * 채권 관련 태그
     */
    BOND("채권", "채권 투자 관련 태그", "#4CAF50"),

    /**
     * 펀드 관련 태그
     */
    FUND("펀드", "펀드 투자 관련 태그", "#FF9800"),

    /**
     * 암호화폐 관련 태그
     */
    CRYPTO("암호화폐", "암호화폐 투자 관련 태그", "#9C27B0"),

    /**
     * 부동산 관련 태그
     */
    REAL_ESTATE("부동산", "부동산 투자 관련 태그", "#795548"),

    /**
     * 경제/거시 관련 태그
     */
    ECONOMICS("경제", "경제/거시 분석 관련 태그", "#607D8B"),

    /**
     * 투자 전략 관련 태그
     */
    STRATEGY("전략", "투자 전략 관련 태그", "#F44336"),

    /**
     * 기술적/기본적 분석 관련 태그
     */
    ANALYSIS("분석", "투자 분석 관련 태그", "#00BCD4"),

    /**
     * 뉴스/시장 동향 관련 태그
     */
    NEWS("뉴스", "시장 뉴스/동향 관련 태그", "#FF5722"),

    /**
     * 튜토리얼/가이드 관련 태그
     */
    TUTORIAL("튜토리얼", "학습 가이드 관련 태그", "#8BC34A"),

    /**
     * 초보자 관련 태그
     */
    BEGINNER("초보자", "초보자 대상 태그", "#FFC107"),

    /**
     * 고급 사용자 관련 태그
     */
    ADVANCED("고급", "고급 사용자 대상 태그", "#E91E63"),

    /**
     * 일반/기타 태그
     */
    GENERAL("일반", "일반/기타 태그", "#9E9E9E");

    private final String displayName;
    private final String description;
    private final String defaultColor;

    TagType(String displayName, String description, String defaultColor) {
        this.displayName = displayName;
        this.description = description;
        this.defaultColor = defaultColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultColor() {
        return defaultColor;
    }

    /**
     * 투자 도메인별 태그인지 확인
     */
    public boolean isInvestmentType() {
        return this == STOCK || this == BOND || this == FUND ||
               this == CRYPTO || this == REAL_ESTATE;
    }

    /**
     * 분석/전략 관련 태그인지 확인
     */
    public boolean isAnalysisType() {
        return this == STRATEGY || this == ANALYSIS;
    }

    /**
     * 교육 관련 태그인지 확인
     */
    public boolean isEducationalType() {
        return this == TUTORIAL || this == BEGINNER || this == ADVANCED;
    }

    /**
     * 정보성 태그인지 확인
     */
    public boolean isInformationalType() {
        return this == NEWS || this == ECONOMICS;
    }

    /**
     * 태그 타입 우선순위 (낮을수록 높은 우선순위)
     */
    public int getPriority() {
        return switch (this) {
            case STOCK -> 1;
            case BOND -> 2;
            case FUND -> 3;
            case CRYPTO -> 4;
            case REAL_ESTATE -> 5;
            case STRATEGY -> 6;
            case ANALYSIS -> 7;
            case ECONOMICS -> 8;
            case NEWS -> 9;
            case TUTORIAL -> 10;
            case BEGINNER -> 11;
            case ADVANCED -> 12;
            case GENERAL -> 13;
        };
    }

    /**
     * 태그 타입별 권장 가중치
     */
    public double getRecommendedWeight() {
        return switch (this) {
            case STOCK, STRATEGY -> 2.0;      // 높은 가중치
            case BOND, FUND -> 1.8;          // 중상 가중치
            case ANALYSIS, ECONOMICS -> 1.5;  // 중간 가중치
            case CRYPTO, REAL_ESTATE -> 1.3; // 중하 가중치
            case NEWS, TUTORIAL -> 1.2;      // 낮은 가중치
            case BEGINNER, ADVANCED -> 1.1;  // 최소 가중치
            case GENERAL -> 1.0;             // 기본 가중치
        };
    }

    /**
     * 타입별 최대 태그 수 제한
     */
    public int getMaxTagsPerArticle() {
        return switch (this) {
            case STOCK, BOND, FUND, CRYPTO, REAL_ESTATE -> 3; // 투자 도메인: 최대 3개
            case STRATEGY, ANALYSIS -> 2;                     // 분석/전략: 최대 2개
            case BEGINNER, ADVANCED -> 1;                     // 레벨: 최대 1개
            default -> 5;                                     // 기타: 최대 5개
        };
    }

    /**
     * 상호 배타적 태그 타입인지 확인
     */
    public boolean isExclusiveWith(TagType other) {
        // 초보자와 고급은 상호 배타적
        if ((this == BEGINNER && other == ADVANCED) ||
            (this == ADVANCED && other == BEGINNER)) {
            return true;
        }

        return false;
    }

    /**
     * 태그 타입별 추천 키워드
     */
    public String[] getRecommendedKeywords() {
        return switch (this) {
            case STOCK -> new String[]{"주식", "종목", "배당", "상장", "IPO", "차트"};
            case BOND -> new String[]{"채권", "국채", "회사채", "수익률", "만기", "금리"};
            case FUND -> new String[]{"펀드", "ETF", "인덱스", "액티브", "패시브", "분산투자"};
            case CRYPTO -> new String[]{"비트코인", "이더리움", "알트코인", "블록체인", "DeFi", "NFT"};
            case REAL_ESTATE -> new String[]{"부동산", "아파트", "상가", "임대", "전세", "REITs"};
            case ECONOMICS -> new String[]{"경제", "GDP", "인플레이션", "금리", "환율", "중앙은행"};
            case STRATEGY -> new String[]{"전략", "포트폴리오", "자산배분", "리밸런싱", "헤지", "리스크"};
            case ANALYSIS -> new String[]{"분석", "차트", "지표", "기술적분석", "기본분석", "밸류에이션"};
            case NEWS -> new String[]{"뉴스", "시장", "동향", "이슈", "실적", "공시"};
            case TUTORIAL -> new String[]{"튜토리얼", "가이드", "방법", "설명", "학습", "교육"};
            case BEGINNER -> new String[]{"초보", "입문", "기초", "처음", "시작", "쉬운"};
            case ADVANCED -> new String[]{"고급", "심화", "전문가", "숙련", "어려운", "복잡한"};
            case GENERAL -> new String[]{"일반", "기타", "정보", "팁", "노하우", "경험"};
        };
    }
}