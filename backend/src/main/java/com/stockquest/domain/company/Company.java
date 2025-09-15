package com.stockquest.domain.company;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 회사 도메인 엔티티
 * 한국 주식 시장의 회사 정보를 나타냅니다.
 */
@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(name = "name_kr", nullable = false, length = 100)
    private String nameKr;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "sector", length = 50)
    private String sector;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "market_cap_display", length = 20)
    private String marketCapDisplay;

    @Column(name = "logo_path", length = 200)
    private String logoPath;

    @Column(name = "description_kr", columnDefinition = "TEXT")
    private String descriptionKr;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "exchange", length = 10)
    private String exchange = "KRX";

    @Column(name = "currency", length = 3)
    private String currency = "KRW";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "popularity_score")
    private Integer popularityScore = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompanyCategoryMapping> categories = new ArrayList<>();

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
    }

    /**
     * 회사 정보 업데이트
     */
    public void updateInfo(String nameKr, String nameEn, String sector, String descriptionKr, String descriptionEn) {
        this.nameKr = nameKr;
        this.nameEn = nameEn;
        this.sector = sector;
        this.descriptionKr = descriptionKr;
        this.descriptionEn = descriptionEn;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 시가총액 업데이트
     */
    public void updateMarketCap(Long marketCap, String marketCapDisplay) {
        this.marketCap = marketCap;
        this.marketCapDisplay = marketCapDisplay;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 인기도 점수 업데이트
     */
    public void updatePopularityScore(Integer score) {
        this.popularityScore = score;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 활성화 상태 토글
     */
    public void toggleActiveStatus() {
        this.isActive = !this.isActive;
        this.updatedAt = LocalDateTime.now();
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
        return categories.stream()
                .anyMatch(mapping -> mapping.getCategoryId().equals(categoryId));
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

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}