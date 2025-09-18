package com.stockquest.domain.content.article;

/**
 * 투자 교육 컨텐츠 난이도
 */
public enum ArticleDifficulty {

    /**
     * 초급 - 투자 입문자를 위한 기초 내용
     */
    BEGINNER("초급", "투자 입문자를 위한 기초 내용", "#4CAF50"),

    /**
     * 중급 - 어느 정도 투자 경험이 있는 사용자를 위한 내용
     */
    INTERMEDIATE("중급", "어느 정도 투자 경험이 있는 사용자를 위한 내용", "#FF9800"),

    /**
     * 고급 - 숙련된 투자자를 위한 고급 전략 및 분석
     */
    ADVANCED("고급", "숙련된 투자자를 위한 고급 전략 및 분석", "#F44336"),

    /**
     * 전문가 - 금융 전문가를 위한 심화 내용
     */
    EXPERT("전문가", "금융 전문가를 위한 심화 내용", "#9C27B0");

    private final String displayName;
    private final String description;
    private final String colorCode;

    ArticleDifficulty(String displayName, String description, String colorCode) {
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getColorCode() {
        return colorCode;
    }

    /**
     * 난이도별 추천 읽기 시간 (분)
     */
    public int getRecommendedReadingTime() {
        return switch (this) {
            case BEGINNER -> 5;      // 초급: 5분 이내 짧은 글
            case INTERMEDIATE -> 10; // 중급: 10분 정도의 글
            case ADVANCED -> 15;     // 고급: 15분 정도의 글
            case EXPERT -> 20;       // 전문가: 20분 이상의 심화 글
        };
    }

    /**
     * 난이도 순서 (낮은 순서부터)
     */
    public int getOrder() {
        return this.ordinal();
    }

    /**
     * 특정 난이도 이하인지 확인
     */
    public boolean isLevelOrBelow(ArticleDifficulty level) {
        return this.getOrder() <= level.getOrder();
    }

    /**
     * 특정 난이도 이상인지 확인
     */
    public boolean isLevelOrAbove(ArticleDifficulty level) {
        return this.getOrder() >= level.getOrder();
    }
}