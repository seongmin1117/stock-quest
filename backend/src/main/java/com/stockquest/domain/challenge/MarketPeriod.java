package com.stockquest.domain.challenge;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 역사적 시장 기간 도메인 엔티티
 * 특정 시장 상황을 나타내는 역사적 기간 정보
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MarketPeriod {
    
    public enum PeriodType {
        CRASH("Market Crash"),
        BULL_MARKET("Bull Market"),  
        BEAR_MARKET("Bear Market"),
        VOLATILITY("High Volatility"),
        RECOVERY("Market Recovery"),
        SECTOR_ROTATION("Sector Rotation");
        
        private final String displayName;
        
        PeriodType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private String name;
    private String description;
    private PeriodType periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String marketRegion;
    private List<String> keyInstruments;  // 주요 투자 대상 ticker 목록
    private String historicalContext;     // 역사적 배경 설명
    private String learningObjectives;    // 학습 목표
    private int difficultyRating;         // 1-10 scale
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 도메인 생성자
    public MarketPeriod(String name, String description, PeriodType periodType,
                       LocalDate startDate, LocalDate endDate) {
        validateBasicInfo(name, startDate, endDate);
        this.name = name;
        this.description = description;
        this.periodType = periodType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
        this.difficultyRating = 5;
        this.marketRegion = "US";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    public void updateBasicInfo(String name, String description, PeriodType periodType,
                               LocalDate startDate, LocalDate endDate) {
        validateBasicInfo(name, startDate, endDate);
        this.name = name;
        this.description = description;
        this.periodType = periodType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDetailInfo(String marketRegion, List<String> keyInstruments,
                               String historicalContext, String learningObjectives,
                               int difficultyRating) {
        this.marketRegion = marketRegion;
        this.keyInstruments = keyInstruments;
        this.historicalContext = historicalContext;
        this.learningObjectives = learningObjectives;
        validateDifficultyRating(difficultyRating);
        this.difficultyRating = difficultyRating;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }

    public boolean isLongTerm() {
        return getDurationInDays() > 365;
    }

    public boolean isCrisisType() {
        return periodType == PeriodType.CRASH || periodType == PeriodType.BEAR_MARKET;
    }

    public boolean isGrowthType() {
        return periodType == PeriodType.BULL_MARKET || periodType == PeriodType.RECOVERY;
    }

    // 검증 메서드
    private void validateBasicInfo(String name, LocalDate startDate, LocalDate endDate) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("시장 기간 이름은 필수입니다.");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("시장 기간 이름은 200자를 초과할 수 없습니다.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("시작 날짜는 필수입니다.");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("종료 날짜는 필수입니다.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이후일 수 없습니다.");
        }
        if (endDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("종료 날짜는 미래 날짜일 수 없습니다.");
        }
    }

    private void validateDifficultyRating(int difficultyRating) {
        if (difficultyRating < 1 || difficultyRating > 10) {
            throw new IllegalArgumentException("난이도 등급은 1-10 사이의 값이어야 합니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketPeriod that = (MarketPeriod) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(periodType, that.periodType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, periodType);
    }

    @Override
    public String toString() {
        return "MarketPeriod{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", periodType=" + periodType +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", marketRegion='" + marketRegion + '\'' +
                ", difficultyRating=" + difficultyRating +
                ", isActive=" + isActive +
                '}';
    }
}