package com.stockquest.domain.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 회사 도메인 엔티티
 * 한국 주식 시장의 회사 정보를 나타냅니다.
 * 헥사고날 아키텍처 준수 - 순수한 비즈니스 로직만 포함
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private Long id;
    private String symbol;
    private String nameKr;
    private String nameEn;
    private String sector;
    private Long marketCap;
    private String marketCapDisplay;
    private String logoPath;
    private String descriptionKr;
    private String descriptionEn;

    @Builder.Default
    private String exchange = "KRX";

    @Builder.Default
    private String currency = "KRW";

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer popularityScore = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<String> categoryIds = new ArrayList<>();

    /**
     * 회사 생성자
     */
    public Company(String symbol, String nameKr, String nameEn, String sector) {
        this.symbol = symbol;
        this.nameKr = nameKr;
        this.nameEn = nameEn;
        this.sector = sector;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.exchange = "KRX";
        this.currency = "KRW";
        this.isActive = true;
        this.popularityScore = 0;
        this.categoryIds = new ArrayList<>();
    }

    /**
     * 회사 정보 업데이트
     */
    public Company updateInfo(String nameKr, String nameEn, String sector, String descriptionKr, String descriptionEn) {
        return this.toBuilder()
                .nameKr(nameKr)
                .nameEn(nameEn)
                .sector(sector)
                .descriptionKr(descriptionKr)
                .descriptionEn(descriptionEn)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 시가총액 업데이트
     */
    public Company updateMarketCap(Long marketCap, String marketCapDisplay) {
        return this.toBuilder()
                .marketCap(marketCap)
                .marketCapDisplay(marketCapDisplay)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 인기도 점수 업데이트
     */
    public Company updatePopularityScore(Integer score) {
        return this.toBuilder()
                .popularityScore(score)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 활성화 상태 토글
     */
    public Company toggleActiveStatus() {
        return this.toBuilder()
                .isActive(!this.isActive)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 카테고리 추가
     */
    public Company addCategory(String categoryId) {
        List<String> newCategoryIds = new ArrayList<>(this.categoryIds);
        if (!newCategoryIds.contains(categoryId)) {
            newCategoryIds.add(categoryId);
        }
        return this.toBuilder()
                .categoryIds(newCategoryIds)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 카테고리 제거
     */
    public Company removeCategory(String categoryId) {
        List<String> newCategoryIds = new ArrayList<>(this.categoryIds);
        newCategoryIds.remove(categoryId);
        return this.toBuilder()
                .categoryIds(newCategoryIds)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 검색용 표시명 반환 (한국어 + 영어)
     */
    public String getDisplayName() {
        return String.format("%s (%s)", nameKr, nameEn);
    }

    /**
     * 회사가 특정 카테고리에 속하는지 확인
     */
    public boolean belongsToCategory(String categoryId) {
        return categoryIds.contains(categoryId);
    }

    /**
     * 시가총액을 읽기 쉬운 형태로 반환
     */
    public String getFormattedMarketCap() {
        if (marketCapDisplay != null && !marketCapDisplay.isEmpty()) {
            return marketCapDisplay;
        }

        if (marketCap == null || marketCap == 0) {
            return "N/A";
        }

        // 조단위 계산
        BigDecimal cap = new BigDecimal(marketCap);
        BigDecimal trillion = new BigDecimal("1000000000000"); // 1조

        if (cap.compareTo(trillion) >= 0) {
            BigDecimal trillions = cap.divide(trillion, 1, BigDecimal.ROUND_HALF_UP);
            return trillions.toString() + "조원";
        } else {
            BigDecimal billion = new BigDecimal("100000000"); // 1억
            BigDecimal billions = cap.divide(billion, 0, BigDecimal.ROUND_HALF_UP);
            return billions.toString() + "억원";
        }
    }

    /**
     * 회사가 한국 시장인지 확인
     */
    public boolean isKoreanMarket() {
        return "KRX".equals(exchange) && "KRW".equals(currency);
    }

    /**
     * 인기 회사인지 확인 (상위 20%)
     */
    public boolean isPopular() {
        return popularityScore != null && popularityScore >= 80;
    }

    /**
     * 대형주인지 확인 (시가총액 1조원 이상)
     */
    public boolean isLargeCap() {
        if (marketCap == null) return false;
        BigDecimal cap = new BigDecimal(marketCap);
        BigDecimal trillion = new BigDecimal("1000000000000"); // 1조
        return cap.compareTo(trillion) >= 0;
    }

    /**
     * 회사 유효성 검증
     */
    public boolean isValid() {
        return symbol != null && !symbol.trim().isEmpty() &&
               nameKr != null && !nameKr.trim().isEmpty() &&
               nameEn != null && !nameEn.trim().isEmpty();
    }

    /**
     * 검색 매칭 점수 계산 (0-100)
     */
    public int calculateSearchScore(String query) {
        if (query == null || query.trim().isEmpty()) {
            return 0;
        }

        String lowerQuery = query.toLowerCase();
        int score = 0;

        // 정확한 심볼 매칭 (최고 점수)
        if (symbol.toLowerCase().equals(lowerQuery)) {
            score += 100;
        } else if (symbol.toLowerCase().contains(lowerQuery)) {
            score += 80;
        }

        // 한국어 이름 매칭
        if (nameKr.toLowerCase().contains(lowerQuery)) {
            score += 60;
        }

        // 영어 이름 매칭
        if (nameEn.toLowerCase().contains(lowerQuery)) {
            score += 50;
        }

        // 섹터 매칭
        if (sector != null && sector.toLowerCase().contains(lowerQuery)) {
            score += 30;
        }

        // 인기도 보너스
        if (isPopular()) {
            score += 10;
        }

        return Math.min(score, 100);
    }
}