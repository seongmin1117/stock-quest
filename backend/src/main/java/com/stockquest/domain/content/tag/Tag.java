package com.stockquest.domain.content.tag;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 블로그 태그 도메인 엔티티
 * 글의 주제와 키워드를 분류하기 위한 태그 시스템
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Tag {

    private Long id;

    /**
     * 태그 이름
     */
    private String name;

    /**
     * URL slug (SEO 친화적 URL)
     */
    private String slug;

    /**
     * 태그 설명
     */
    private String description;

    /**
     * 태그 색상 (Hex 코드)
     */
    private String colorCode;

    /**
     * 태그 타입 (카테고리별 분류)
     */
    private TagType type;

    /**
     * 활성화 여부
     */
    @Builder.Default
    private boolean active = true;

    /**
     * 이 태그를 사용하는 글 수 (캐시된 값)
     */
    @Builder.Default
    private Long usageCount = 0L;

    /**
     * 인기 태그 여부
     */
    @Builder.Default
    private boolean popular = false;

    /**
     * 추천 태그 여부 (자동 완성에서 우선 노출)
     */
    @Builder.Default
    private boolean suggested = false;

    /**
     * 태그 가중치 (검색 및 추천 알고리즘에서 사용)
     */
    @Builder.Default
    private Double weight = 1.0;

    /**
     * 생성일
     */
    private LocalDateTime createdAt;

    /**
     * 수정일
     */
    private LocalDateTime updatedAt;

    /**
     * 도메인 생성자
     */
    public Tag(String name, String description, TagType type, String colorCode) {
        validateName(name);
        validateDescription(description);
        validateColorCode(colorCode);

        this.name = name;
        this.slug = generateSlug(name);
        this.description = description;
        this.type = type != null ? type : TagType.GENERAL;
        this.colorCode = colorCode;
        this.active = true;
        this.usageCount = 0L;
        this.popular = false;
        this.suggested = false;
        this.weight = 1.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("태그 이름은 필수입니다");
        }
        if (name.length() > 30) {
            throw new IllegalArgumentException("태그 이름은 30자를 초과할 수 없습니다");
        }
        if (name.contains(" ")) {
            throw new IllegalArgumentException("태그 이름에는 공백을 포함할 수 없습니다");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("태그 설명은 200자를 초과할 수 없습니다");
        }
    }

    private void validateColorCode(String colorCode) {
        if (colorCode != null && !colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("색상 코드는 #RRGGBB 형식이어야 합니다");
        }
    }

    /**
     * 이름으로부터 URL slug 생성
     */
    private String generateSlug(String name) {
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9가-힣\\-]", "")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }

    /**
     * 태그 정보 수정
     */
    public void update(String name, String description, TagType type, String colorCode) {
        validateName(name);
        validateDescription(description);
        validateColorCode(colorCode);

        this.name = name;
        this.slug = generateSlug(name);
        this.description = description;
        this.type = type != null ? type : TagType.GENERAL;
        this.colorCode = colorCode;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 태그 활성화/비활성화
     */
    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용 횟수 업데이트
     */
    public void updateUsageCount(Long usageCount) {
        if (usageCount == null || usageCount < 0) {
            throw new IllegalArgumentException("사용 횟수는 0 이상이어야 합니다");
        }

        this.usageCount = usageCount;
        this.updatedAt = LocalDateTime.now();

        // 사용 횟수에 따라 인기 태그 자동 설정
        updatePopularStatus();
    }

    /**
     * 사용 횟수 증가
     */
    public void incrementUsageCount() {
        this.usageCount++;
        this.updatedAt = LocalDateTime.now();
        updatePopularStatus();
    }

    /**
     * 사용 횟수 감소
     */
    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
            this.updatedAt = LocalDateTime.now();
            updatePopularStatus();
        }
    }

    /**
     * 인기 태그 상태 자동 업데이트
     */
    private void updatePopularStatus() {
        // 사용 횟수가 10회 이상이면 인기 태그로 설정
        this.popular = this.usageCount >= 10;
    }

    /**
     * 인기 태그 설정
     */
    public void setPopular(boolean popular) {
        this.popular = popular;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 추천 태그 설정
     */
    public void setSuggested(boolean suggested) {
        this.suggested = suggested;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 태그 가중치 설정
     */
    public void setWeight(Double weight) {
        if (weight == null || weight < 0.0 || weight > 10.0) {
            throw new IllegalArgumentException("가중치는 0.0 ~ 10.0 사이여야 합니다");
        }

        this.weight = weight;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 활성화된 태그인지 확인
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * 사용 중인 태그인지 확인
     */
    public boolean isInUse() {
        return this.usageCount > 0;
    }

    /**
     * 인기 태그인지 확인
     */
    public boolean isPopular() {
        return this.popular;
    }

    /**
     * 추천 태그인지 확인
     */
    public boolean isSuggested() {
        return this.suggested;
    }

    /**
     * 기본 색상 코드 반환 (설정되지 않은 경우)
     */
    public String getEffectiveColorCode() {
        if (this.colorCode != null && !this.colorCode.trim().isEmpty()) {
            return this.colorCode;
        }

        // 태그 타입에 따른 기본 색상
        return switch (this.type) {
            case STOCK -> "#2196F3";      // 파란색
            case BOND -> "#4CAF50";       // 초록색
            case FUND -> "#FF9800";       // 주황색
            case CRYPTO -> "#9C27B0";     // 보라색
            case REAL_ESTATE -> "#795548"; // 갈색
            case ECONOMICS -> "#607D8B";   // 청회색
            case STRATEGY -> "#F44336";    // 빨간색
            case ANALYSIS -> "#00BCD4";    // 청록색
            case NEWS -> "#FF5722";        // 진한 주황색
            case TUTORIAL -> "#8BC34A";    // 연두색
            case BEGINNER -> "#FFC107";    // 노란색
            case ADVANCED -> "#E91E63";    // 핑크색
            case GENERAL -> "#9E9E9E";     // 회색
        };
    }

    /**
     * 태그 표시명 반환 (# 포함)
     */
    public String getDisplayName() {
        return "#" + this.name;
    }

    /**
     * 중요도 점수 계산 (가중치 × 사용 횟수)
     */
    public Double getImportanceScore() {
        return this.weight * Math.log(1 + this.usageCount);
    }

    /**
     * 태그 순위 계산 (인기도 + 가중치)
     */
    public Double getPopularityScore() {
        double baseScore = this.usageCount * this.weight;

        // 인기 태그 보너스
        if (this.popular) {
            baseScore *= 1.5;
        }

        // 추천 태그 보너스
        if (this.suggested) {
            baseScore *= 1.2;
        }

        return baseScore;
    }

    /**
     * 태그 레벨 계산 (사용 횟수 기반)
     */
    public TagLevel getTagLevel() {
        if (this.usageCount >= 100) {
            return TagLevel.LEGENDARY;
        } else if (this.usageCount >= 50) {
            return TagLevel.MASTER;
        } else if (this.usageCount >= 20) {
            return TagLevel.EXPERT;
        } else if (this.usageCount >= 10) {
            return TagLevel.INTERMEDIATE;
        } else if (this.usageCount >= 5) {
            return TagLevel.NOVICE;
        } else {
            return TagLevel.BEGINNER;
        }
    }

    /**
     * 태그 레벨 Enum
     */
    public enum TagLevel {
        BEGINNER("초보", "#9E9E9E"),
        NOVICE("입문", "#607D8B"),
        INTERMEDIATE("중급", "#2196F3"),
        EXPERT("숙련", "#4CAF50"),
        MASTER("마스터", "#FF9800"),
        LEGENDARY("전설", "#9C27B0");

        private final String displayName;
        private final String colorCode;

        TagLevel(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColorCode() {
            return colorCode;
        }
    }
}