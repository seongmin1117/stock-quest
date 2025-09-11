package com.stockquest.application.admin.challenge;

import com.stockquest.application.admin.challenge.dto.ChallengeDetailResult;
import com.stockquest.application.admin.challenge.port.in.CreateChallengeUseCase;
import com.stockquest.domain.challenge.*;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.challenge.port.ChallengeCategoryRepository;
import com.stockquest.domain.challenge.port.ChallengeTemplateRepository;
import com.stockquest.application.exception.ChallengeNotFoundException;
import com.stockquest.application.exception.InvalidChallengeStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 챌린지 생성 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateChallengeService implements CreateChallengeUseCase {
    
    private final ChallengeRepository challengeRepository;
    private final ChallengeCategoryRepository categoryRepository;
    private final ChallengeTemplateRepository templateRepository;
    private final ChallengeValidationService validationService;
    
    @Override
    public ChallengeDetailResult create(CreateChallengeCommand command) {
        log.info("Creating challenge with title: {}", command.getTitle());
        
        // 유효성 검증
        validateCreateCommand(command);
        
        // 챌린지 엔티티 생성
        Challenge challenge = createChallengeEntity(command);
        
        // 투자 상품 추가
        List<ChallengeInstrument> instruments = createInstruments(command.getInstruments(), challenge.getId());
        challenge = challenge.toBuilder()
                .instruments(instruments)
                .build();
        
        // 저장
        Challenge savedChallenge = challengeRepository.save(challenge);
        
        log.info("Challenge created successfully with id: {}", savedChallenge.getId());
        return mapToDetailResult(savedChallenge);
    }
    
    @Override
    public ChallengeDetailResult createFromTemplate(CreateFromTemplateCommand command) {
        log.info("Creating challenge from template id: {}", command.getTemplateId());
        
        // 템플릿 조회
        ChallengeTemplate template = templateRepository.findById(command.getTemplateId())
                .orElseThrow(() -> new ChallengeNotFoundException("Template not found: " + command.getTemplateId()));
        
        if (!template.isActive()) {
            throw new InvalidChallengeStateException("Template is not active: " + command.getTemplateId());
        }
        
        // 템플릿으로부터 챌린지 생성
        Challenge challenge = template.createChallenge();
        
        // 커스터마이제이션 적용
        if (command.getTitle() != null) {
            challenge = challenge.toBuilder().title(command.getTitle()).build();
        }
        if (command.getDescription() != null) {
            challenge = challenge.toBuilder().description(command.getDescription()).build();
        }
        
        // 추가 커스터마이제이션 적용
        challenge = applyCustomizations(challenge, command.getCustomizations());
        
        // 생성자 설정
        challenge = challenge.toBuilder()
                .createdBy(command.getAdminId())
                .createdAt(LocalDateTime.now())
                .build();
        
        // 저장
        Challenge savedChallenge = challengeRepository.save(challenge);
        
        log.info("Challenge created from template successfully with id: {}", savedChallenge.getId());
        return mapToDetailResult(savedChallenge);
    }
    
    private void validateCreateCommand(CreateChallengeCommand command) {
        // 카테고리 존재 여부 확인
        if (!categoryRepository.existsById(command.getCategoryId())) {
            throw new ChallengeNotFoundException("Category not found: " + command.getCategoryId());
        }
        
        // 템플릿 존재 여부 확인 (선택적)
        if (command.getTemplateId() != null && !templateRepository.existsById(command.getTemplateId())) {
            throw new ChallengeNotFoundException("Template not found: " + command.getTemplateId());
        }
        
        // 기간 유효성 검증
        if (command.getPeriodEnd().isBefore(command.getPeriodStart())) {
            throw new InvalidChallengeStateException("End date must be after start date");
        }
        
        // 기간 길이 검증 (최소 1일, 최대 5년)
        long daysBetween = ChronoUnit.DAYS.between(command.getPeriodStart(), command.getPeriodEnd());
        if (daysBetween < 1) {
            throw new InvalidChallengeStateException("Challenge period must be at least 1 day");
        }
        if (daysBetween > 1825) { // 5년
            throw new InvalidChallengeStateException("Challenge period cannot exceed 5 years");
        }
        
        // 투자 상품 유효성 검증
        validationService.validateInstruments(command.getInstruments());
    }
    
    private Challenge createChallengeEntity(CreateChallengeCommand command) {
        LocalDateTime now = LocalDateTime.now();
        
        // 기간 일수 계산
        int durationDays = (int) ChronoUnit.DAYS.between(command.getPeriodStart(), command.getPeriodEnd());
        
        return Challenge.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .categoryId(command.getCategoryId())
                .templateId(command.getTemplateId())
                .marketPeriodId(command.getMarketPeriodId())
                .difficulty(command.getDifficulty())
                .challengeType(command.getChallengeType())
                .status(ChallengeStatus.DRAFT)
                .initialBalance(command.getInitialBalance())
                .estimatedDurationMinutes(command.getEstimatedDurationMinutes())
                .durationDays(durationDays)
                .periodStart(command.getPeriodStart())
                .periodEnd(command.getPeriodEnd())
                .speedFactor(command.getSpeedFactor())
                .tags(command.getTags())
                .successCriteria(command.getSuccessCriteria())
                .marketScenario(command.getMarketScenario())
                .learningObjectives(command.getLearningObjectives())
                .maxParticipants(command.getMaxParticipants())
                .currentParticipants(0)
                .createdBy(command.getAdminId())
                .isFeatured(false)
                .sortOrder(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    private List<ChallengeInstrument> createInstruments(List<CreateChallengeCommand.ChallengeInstrumentCommand> instrumentCommands, Long challengeId) {
        List<ChallengeInstrument> instruments = new ArrayList<>();
        
        for (CreateChallengeCommand.ChallengeInstrumentCommand cmd : instrumentCommands) {
            ChallengeInstrument instrument = ChallengeInstrument.builder()
                    .challengeId(challengeId)
                    .instrumentKey(cmd.getInstrumentKey())
                    .actualTicker(cmd.getActualTicker())
                    .hiddenName(cmd.getHiddenName())
                    .actualName(cmd.getActualName())
                    .type(ChallengeInstrument.InstrumentType.valueOf(cmd.getType()))
                    .build();
            instruments.add(instrument);
        }
        
        return instruments;
    }
    
    private Challenge applyCustomizations(Challenge challenge, Map<String, Object> customizations) {
        if (customizations == null || customizations.isEmpty()) {
            return challenge;
        }
        
        Challenge.ChallengeBuilder builder = challenge.toBuilder();
        
        // 초기 자본금 커스터마이제이션
        if (customizations.containsKey("initialBalance")) {
            builder.initialBalance(new java.math.BigDecimal(customizations.get("initialBalance").toString()));
        }
        
        // 시간 압축 배율 커스터마이제이션
        if (customizations.containsKey("speedFactor")) {
            builder.speedFactor((Integer) customizations.get("speedFactor"));
        }
        
        // 최대 참여자 수 커스터마이제이션
        if (customizations.containsKey("maxParticipants")) {
            builder.maxParticipants((Integer) customizations.get("maxParticipants"));
        }
        
        return builder.build();
    }
    
    private ChallengeDetailResult mapToDetailResult(Challenge challenge) {
        return ChallengeDetailResult.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .categoryId(challenge.getCategoryId())
                .templateId(challenge.getTemplateId())
                .marketPeriodId(challenge.getMarketPeriodId())
                .difficulty(challenge.getDifficulty())
                .challengeType(challenge.getChallengeType())
                .status(challenge.getStatus())
                .initialBalance(challenge.getInitialBalance())
                .estimatedDurationMinutes(challenge.getEstimatedDurationMinutes())
                .periodStart(challenge.getPeriodStart())
                .periodEnd(challenge.getPeriodEnd())
                .speedFactor(challenge.getSpeedFactor())
                .tags(challenge.getTags())
                .successCriteria(challenge.getSuccessCriteria())
                .marketScenario(challenge.getMarketScenario())
                .learningObjectives(challenge.getLearningObjectives())
                .maxParticipants(challenge.getMaxParticipants())
                .currentParticipants(challenge.getCurrentParticipants())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .createdBy(challenge.getCreatedBy())
                .isFeatured(challenge.getIsFeatured())
                .sortOrder(challenge.getSortOrder())
                .createdAt(challenge.getCreatedAt())
                .updatedAt(challenge.getUpdatedAt())
                .instruments(challenge.getInstruments())
                .build();
    }
}