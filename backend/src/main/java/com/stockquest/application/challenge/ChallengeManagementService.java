package com.stockquest.application.challenge;

import com.stockquest.domain.challenge.*;
import com.stockquest.application.challenge.dto.*;
import com.stockquest.application.port.out.ChallengePort;
import com.stockquest.application.port.out.ChallengeCategoryPort;
import com.stockquest.application.port.out.ChallengeTemplatePort;
import com.stockquest.application.port.out.MarketPeriodPort;
import com.stockquest.application.auth.AuthorizationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 챌린지 관리 서비스
 * 챌린지의 CRUD 작업, 템플릿 기반 생성, 라이프사이클 관리를 담당
 */
@Service
@Transactional
public class ChallengeManagementService {

    private final ChallengePort challengePort;
    private final ChallengeCategoryPort categoryPort;
    private final ChallengeTemplatePort templatePort;
    private final MarketPeriodPort marketPeriodPort;
    private final AuthorizationService authorizationService;

    public ChallengeManagementService(ChallengePort challengePort,
                                    ChallengeCategoryPort categoryPort,
                                    ChallengeTemplatePort templatePort,
                                    MarketPeriodPort marketPeriodPort,
                                    AuthorizationService authorizationService) {
        this.challengePort = challengePort;
        this.categoryPort = categoryPort;
        this.templatePort = templatePort;
        this.marketPeriodPort = marketPeriodPort;
        this.authorizationService = authorizationService;
    }

    /**
     * 새로운 챌린지 생성 (from scratch)
     */
    public Challenge createChallenge(CreateChallengeCommand command) {
        validateCreateCommand(command);
        
        Challenge challenge = Challenge.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .categoryId(command.getCategoryId())
                .difficulty(command.getDifficulty())
                .challengeType(command.getChallengeType())
                .status(ChallengeStatus.DRAFT)
                .initialBalance(command.getInitialBalance())
                .durationDays(command.getDurationDays())
                .maxParticipants(command.getMaxParticipants())
                .currentParticipants(0)
                .availableInstruments(command.getAvailableInstruments())
                .tradingRestrictions(command.getTradingRestrictions())
                .successCriteria(command.getSuccessCriteria())
                .learningObjectives(command.getLearningObjectives())
                .marketScenarioDescription(command.getMarketScenarioDescription())
                .riskLevel(command.getRiskLevel())
                .estimatedTimeMinutes(command.getEstimatedTimeMinutes())
                .tags(command.getTags())
                .createdBy(command.getCreatedBy())
                .averageRating(BigDecimal.ZERO)
                .totalReviews(0)
                .featured(false)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return challengePort.save(challenge);
    }

    /**
     * 템플릿을 기반으로 챌린지 생성
     */
    public Challenge createChallengeFromTemplate(CreateFromTemplateCommand command) {
        ChallengeTemplate template = templatePort.findById(command.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + command.getTemplateId()));

        if (!template.isActive()) {
            throw new IllegalArgumentException("비활성화된 템플릿입니다.");
        }

        Challenge challenge = Challenge.builder()
                .title(command.getTitle() != null ? command.getTitle() : template.getName())
                .description(command.getDescription() != null ? command.getDescription() : template.getDescription())
                .categoryId(template.getCategoryId())
                .templateId(template.getId())
                .marketPeriodId(template.getMarketPeriodId())
                .difficulty(command.getDifficulty() != null ? command.getDifficulty() : template.getDifficulty())
                .challengeType(command.getChallengeType())
                .status(ChallengeStatus.DRAFT)
                .initialBalance(command.getInitialBalance() != null ? command.getInitialBalance() : template.getInitialBalance())
                .durationDays(command.getDurationDays() != null ? command.getDurationDays() : template.getDurationDays())
                .maxParticipants(command.getMaxParticipants())
                .currentParticipants(0)
                .availableInstruments(template.getAvailableInstruments())
                .tradingRestrictions(template.getTradingRestrictions())
                .successCriteria(template.getSuccessCriteria())
                .learningObjectives(template.getLearningObjectives())
                .marketScenarioDescription(template.getMarketScenarioDescription())
                .riskLevel(template.getRiskLevel())
                .estimatedTimeMinutes(template.getEstimatedCompletionTime())
                .tags(template.getTags())
                .createdBy(command.getCreatedBy())
                .averageRating(BigDecimal.ZERO)
                .totalReviews(0)
                .featured(false)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Challenge savedChallenge = challengePort.save(challenge);
        
        // 템플릿 사용 횟수 증가
        template.incrementUsageCount();
        templatePort.save(template);

        return savedChallenge;
    }

    /**
     * 챌린지 수정
     */
    public Challenge updateChallenge(UpdateChallengeCommand command) {
        Challenge challenge = challengePort.findById(command.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + command.getChallengeId()));

        // 수정 권한 확인
        if (!canModifyChallenge(challenge, command.getModifiedBy())) {
            throw new IllegalArgumentException("챌린지를 수정할 권한이 없습니다.");
        }

        // 상태에 따른 수정 가능 여부 확인
        if (challenge.getStatus() == ChallengeStatus.ACTIVE || challenge.getStatus() == ChallengeStatus.COMPLETED) {
            throw new IllegalArgumentException("활성 또는 완료된 챌린지는 수정할 수 없습니다.");
        }

        // 필드 업데이트
        Challenge updatedChallenge = challenge.toBuilder()
                .title(command.getTitle())
                .description(command.getDescription())
                .categoryId(command.getCategoryId())
                .difficulty(command.getDifficulty())
                .initialBalance(command.getInitialBalance())
                .durationDays(command.getDurationDays())
                .maxParticipants(command.getMaxParticipants())
                .availableInstruments(command.getAvailableInstruments())
                .tradingRestrictions(command.getTradingRestrictions())
                .successCriteria(command.getSuccessCriteria())
                .learningObjectives(command.getLearningObjectives())
                .marketScenarioDescription(command.getMarketScenarioDescription())
                .riskLevel(command.getRiskLevel())
                .estimatedTimeMinutes(command.getEstimatedTimeMinutes())
                .tags(command.getTags())
                .lastModifiedBy(command.getModifiedBy())
                .updatedAt(LocalDateTime.now())
                .build();
        
        updatedChallenge.incrementVersion();

        return challengePort.save(updatedChallenge);
    }

    /**
     * 챌린지 상태 변경
     */
    public Challenge changeStatus(Long challengeId, ChallengeStatus newStatus, Long modifiedBy) {
        Challenge challenge = challengePort.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + challengeId));

        if (!challenge.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                String.format("챌린지 상태를 %s에서 %s로 변경할 수 없습니다.", challenge.getStatus(), newStatus)
            );
        }

        Challenge updatedChallenge = challenge.toBuilder()
                .status(newStatus)
                .lastModifiedBy(modifiedBy)
                .updatedAt(LocalDateTime.now())
                .build();
        
        updatedChallenge.incrementVersion();

        return challengePort.save(updatedChallenge);
    }

    /**
     * 챌린지 활성화
     */
    public Challenge activateChallenge(Long challengeId, Long modifiedBy) {
        Challenge challenge = changeStatus(challengeId, ChallengeStatus.ACTIVE, modifiedBy);
        
        // 활성화 시 필요한 추가 로직
        validateChallengeForActivation(challenge);
        
        return challenge;
    }

    /**
     * 챌린지 완료 처리
     */
    public Challenge completeChallenge(Long challengeId, Long modifiedBy) {
        return changeStatus(challengeId, ChallengeStatus.COMPLETED, modifiedBy);
    }

    /**
     * 챌린지 삭제 (소프트 삭제 - 아카이브)
     */
    public Challenge archiveChallenge(Long challengeId, Long modifiedBy) {
        Challenge challenge = challengePort.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + challengeId));

        if (challenge.getStatus() == ChallengeStatus.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 챌린지는 아카이브할 수 없습니다.");
        }

        return changeStatus(challengeId, ChallengeStatus.ARCHIVED, modifiedBy);
    }

    /**
     * 피처드 챌린지 설정
     */
    public Challenge setFeaturedChallenge(Long challengeId, boolean featured, Long modifiedBy) {
        Challenge challenge = challengePort.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + challengeId));

        Challenge updatedChallenge = challenge.toBuilder()
                .featured(featured)
                .lastModifiedBy(modifiedBy)
                .updatedAt(LocalDateTime.now())
                .build();
        
        updatedChallenge.incrementVersion();

        return challengePort.save(updatedChallenge);
    }

    /**
     * 챌린지 목록 조회 (필터링 및 페이징)
     */
    @Transactional(readOnly = true)
    public ChallengePage getChallenges(ChallengeSearchCriteria criteria) {
        return challengePort.findByCriteria(criteria);
    }

    /**
     * 챌린지 상세 조회
     */
    @Transactional(readOnly = true)
    public Optional<Challenge> getChallengeById(Long challengeId) {
        return challengePort.findById(challengeId);
    }

    /**
     * 카테고리별 챌린지 조회
     */
    @Transactional(readOnly = true)
    public List<Challenge> getChallengesByCategory(Long categoryId) {
        return challengePort.findByCategoryId(categoryId);
    }

    /**
     * 템플릿별 챌린지 조회
     */
    @Transactional(readOnly = true)
    public List<Challenge> getChallengesByTemplate(Long templateId) {
        return challengePort.findByTemplateId(templateId);
    }

    /**
     * 인기 챌린지 조회
     */
    @Transactional(readOnly = true)
    public List<Challenge> getPopularChallenges(int limit) {
        return challengePort.findPopularChallenges(limit);
    }

    /**
     * 피처드 챌린지 조회
     */
    @Transactional(readOnly = true)
    public List<Challenge> getFeaturedChallenges() {
        return challengePort.findFeaturedChallenges();
    }

    /**
     * 챌린지 복제
     */
    public Challenge cloneChallenge(Long challengeId, String newTitle, Long createdBy) {
        Challenge original = challengePort.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("원본 챌린지를 찾을 수 없습니다: " + challengeId));

        Challenge cloned = original.toBuilder()
                .id(null) // 새로운 ID 생성을 위해 null
                .title(newTitle)
                .status(ChallengeStatus.DRAFT)
                .currentParticipants(0)
                .createdBy(createdBy)
                .lastModifiedBy(null)
                .averageRating(BigDecimal.ZERO)
                .totalReviews(0)
                .featured(false)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return challengePort.save(cloned);
    }

    // 검증 메서드들
    private void validateCreateCommand(CreateChallengeCommand command) {
        if (command.getTitle() == null || command.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("챌린지 제목은 필수입니다.");
        }
        if (command.getCategoryId() != null) {
            categoryPort.findById(command.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + command.getCategoryId()));
        }
        if (command.getInitialBalance() == null || command.getInitialBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("초기 자본은 0보다 커야 합니다.");
        }
        if (command.getDurationDays() == null || command.getDurationDays() <= 0) {
            throw new IllegalArgumentException("챌린지 기간은 0보다 커야 합니다.");
        }
    }

    private boolean canModifyChallenge(Challenge challenge, Long userId) {
        // AuthorizationService를 통해 권한 검증
        return authorizationService.canModifyChallenge(userId, challenge.getCreatedBy());
    }

    private void validateChallengeForActivation(Challenge challenge) {
        if (challenge.getAvailableInstruments() == null || challenge.getAvailableInstruments().isEmpty()) {
            throw new IllegalArgumentException("활성화하려면 거래 가능한 종목이 설정되어야 합니다.");
        }
        if (challenge.getSuccessCriteria() == null || challenge.getSuccessCriteria().isEmpty()) {
            throw new IllegalArgumentException("활성화하려면 성공 기준이 설정되어야 합니다.");
        }
    }
}