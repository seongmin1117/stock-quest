package com.stockquest.application.challenge.dto;

import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.ChallengeType;

import java.math.BigDecimal;
import java.util.List;

/**
 * 챌린지 검색 조건 객체
 */
public class ChallengeSearchCriteria {
    private String title;
    private Long categoryId;
    private Long templateId;
    private ChallengeDifficulty difficulty;
    private ChallengeType challengeType;
    private ChallengeStatus status;
    private Long createdBy;
    private Boolean featured;
    private List<String> tags;
    private Integer minRiskLevel;
    private Integer maxRiskLevel;
    private Integer minDurationDays;
    private Integer maxDurationDays;
    private BigDecimal minAverageRating;
    private Integer minParticipants;
    private Integer maxParticipants;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;

    // 기본 생성자
    public ChallengeSearchCriteria() {
        this.page = 0;
        this.size = 20;
        this.sortBy = "createdAt";
        this.sortDirection = "DESC";
    }

    // 빌더 패턴을 위한 정적 메서드
    public static ChallengeSearchCriteriaBuilder builder() {
        return new ChallengeSearchCriteriaBuilder();
    }

    // Getter methods
    public String getTitle() {
        return title;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public ChallengeDifficulty getDifficulty() {
        return difficulty;
    }

    public ChallengeType getChallengeType() {
        return challengeType;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public List<String> getTags() {
        return tags;
    }

    public Integer getMinRiskLevel() {
        return minRiskLevel;
    }

    public Integer getMaxRiskLevel() {
        return maxRiskLevel;
    }

    public Integer getMinDurationDays() {
        return minDurationDays;
    }

    public Integer getMaxDurationDays() {
        return maxDurationDays;
    }

    public BigDecimal getMinAverageRating() {
        return minAverageRating;
    }

    public Integer getMinParticipants() {
        return minParticipants;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    // Setter methods
    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public void setDifficulty(ChallengeDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setChallengeType(ChallengeType challengeType) {
        this.challengeType = challengeType;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setMinRiskLevel(Integer minRiskLevel) {
        this.minRiskLevel = minRiskLevel;
    }

    public void setMaxRiskLevel(Integer maxRiskLevel) {
        this.maxRiskLevel = maxRiskLevel;
    }

    public void setMinDurationDays(Integer minDurationDays) {
        this.minDurationDays = minDurationDays;
    }

    public void setMaxDurationDays(Integer maxDurationDays) {
        this.maxDurationDays = maxDurationDays;
    }

    public void setMinAverageRating(BigDecimal minAverageRating) {
        this.minAverageRating = minAverageRating;
    }

    public void setMinParticipants(Integer minParticipants) {
        this.minParticipants = minParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    // Builder class
    public static class ChallengeSearchCriteriaBuilder {
        private ChallengeSearchCriteria criteria = new ChallengeSearchCriteria();

        public ChallengeSearchCriteriaBuilder title(String title) {
            criteria.setTitle(title);
            return this;
        }

        public ChallengeSearchCriteriaBuilder categoryId(Long categoryId) {
            criteria.setCategoryId(categoryId);
            return this;
        }

        public ChallengeSearchCriteriaBuilder templateId(Long templateId) {
            criteria.setTemplateId(templateId);
            return this;
        }

        public ChallengeSearchCriteriaBuilder difficulty(ChallengeDifficulty difficulty) {
            criteria.setDifficulty(difficulty);
            return this;
        }

        public ChallengeSearchCriteriaBuilder challengeType(ChallengeType challengeType) {
            criteria.setChallengeType(challengeType);
            return this;
        }

        public ChallengeSearchCriteriaBuilder status(ChallengeStatus status) {
            criteria.setStatus(status);
            return this;
        }

        public ChallengeSearchCriteriaBuilder createdBy(Long createdBy) {
            criteria.setCreatedBy(createdBy);
            return this;
        }

        public ChallengeSearchCriteriaBuilder featured(Boolean featured) {
            criteria.setFeatured(featured);
            return this;
        }

        public ChallengeSearchCriteriaBuilder tags(List<String> tags) {
            criteria.setTags(tags);
            return this;
        }

        public ChallengeSearchCriteriaBuilder riskLevel(Integer min, Integer max) {
            criteria.setMinRiskLevel(min);
            criteria.setMaxRiskLevel(max);
            return this;
        }

        public ChallengeSearchCriteriaBuilder durationDays(Integer min, Integer max) {
            criteria.setMinDurationDays(min);
            criteria.setMaxDurationDays(max);
            return this;
        }

        public ChallengeSearchCriteriaBuilder minAverageRating(BigDecimal minRating) {
            criteria.setMinAverageRating(minRating);
            return this;
        }

        public ChallengeSearchCriteriaBuilder participants(Integer min, Integer max) {
            criteria.setMinParticipants(min);
            criteria.setMaxParticipants(max);
            return this;
        }

        public ChallengeSearchCriteriaBuilder sort(String sortBy, String direction) {
            criteria.setSortBy(sortBy);
            criteria.setSortDirection(direction);
            return this;
        }

        public ChallengeSearchCriteriaBuilder page(Integer page, Integer size) {
            criteria.setPage(page);
            criteria.setSize(size);
            return this;
        }

        public ChallengeSearchCriteria build() {
            return criteria;
        }
    }
}