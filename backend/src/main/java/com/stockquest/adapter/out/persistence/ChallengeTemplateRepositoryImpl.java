package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.ChallengeTemplateJpaEntity;
import com.stockquest.adapter.out.persistence.repository.ChallengeTemplateJpaRepository;
import com.stockquest.domain.challenge.ChallengeTemplate;
import com.stockquest.domain.challenge.ChallengeType;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.port.ChallengeTemplateRepository;
import lombok.RequiredArgsConstructor;
import com.stockquest.domain.common.Page;
import com.stockquest.domain.common.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ChallengeTemplateRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ChallengeTemplateRepositoryImpl implements ChallengeTemplateRepository {
    
    private final ChallengeTemplateJpaRepository jpaRepository;
    
    @Override
    public ChallengeTemplate save(ChallengeTemplate template) {
        ChallengeTemplateJpaEntity entity = toEntity(template);
        ChallengeTemplateJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Optional<ChallengeTemplate> findById(Long id) {
        return jpaRepository.findById(id)
            .map(this::toDomain);
    }
    
    @Override
    public List<ChallengeTemplate> findAllActive() {
        return jpaRepository.findAllActive()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ChallengeTemplate> findAll(PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest =
            org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize()
            );

        org.springframework.data.domain.Page<ChallengeTemplateJpaEntity> springPage =
            jpaRepository.findAll(springPageRequest);

        List<ChallengeTemplate> content = springPage.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());

        return new Page<>(content, pageRequest, springPage.getTotalElements());
    }
    
    @Override
    public List<ChallengeTemplate> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryId(categoryId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeTemplate> findByTemplateType(ChallengeType templateType) {
        return jpaRepository.findByTemplateType(templateType)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeTemplate> findByDifficulty(ChallengeDifficulty difficulty) {
        return jpaRepository.findByDifficulty(difficulty)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeTemplate> findByTagsContaining(String tag) {
        return jpaRepository.findByTagsContaining(tag)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeTemplate> findByNameContaining(String name) {
        return jpaRepository.findByNameContaining(name)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeTemplate> findByCreatedBy(Long userId) {
        return jpaRepository.findByCreatedBy(userId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(ChallengeTemplate template) {
        ChallengeTemplateJpaEntity entity = toEntity(template);
        jpaRepository.delete(entity);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
    
    // 엔티티 변환 메서드들
    private ChallengeTemplate toDomain(ChallengeTemplateJpaEntity entity) {
        return ChallengeTemplate.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .categoryId(entity.getCategoryId())
            .difficulty(entity.getDifficulty())
            .templateType(entity.getTemplateType())
            .initialBalance(entity.getInitialBalance())
            .estimatedDurationMinutes(entity.getEstimatedDurationMinutes())
            .speedFactor(entity.getSpeedFactor())
            .config(entity.getConfig())
            .tags(entity.getTags())
            .successCriteria(entity.getSuccessCriteria())
            .marketScenario(entity.getMarketScenario())
            .learningObjectives(entity.getLearningObjectives())
            .isActive(entity.getIsActive())
            .createdBy(entity.getCreatedBy())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    private ChallengeTemplateJpaEntity toEntity(ChallengeTemplate domain) {
        ChallengeTemplateJpaEntity entity = new ChallengeTemplateJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setCategoryId(domain.getCategoryId());
        entity.setDifficulty(domain.getDifficulty());
        entity.setTemplateType(domain.getTemplateType());
        entity.setInitialBalance(domain.getInitialBalance());
        entity.setEstimatedDurationMinutes(domain.getEstimatedDurationMinutes());
        entity.setSpeedFactor(domain.getSpeedFactor());
        entity.setConfig(domain.getConfig());
        entity.setTags(domain.getTags());
        entity.setSuccessCriteria(domain.getSuccessCriteria());
        entity.setMarketScenario(domain.getMarketScenario());
        entity.setLearningObjectives(domain.getLearningObjectives());
        entity.setIsActive(domain.isActive());
        entity.setCreatedBy(domain.getCreatedBy());
        return entity;
    }
}