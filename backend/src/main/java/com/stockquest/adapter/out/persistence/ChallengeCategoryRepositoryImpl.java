package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.ChallengeCategoryJpaEntity;
import com.stockquest.adapter.out.persistence.repository.ChallengeCategoryJpaRepository;
import com.stockquest.domain.challenge.ChallengeCategory;
import com.stockquest.domain.challenge.port.ChallengeCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ChallengeCategoryRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ChallengeCategoryRepositoryImpl implements ChallengeCategoryRepository {
    
    private final ChallengeCategoryJpaRepository jpaRepository;
    
    @Override
    public ChallengeCategory save(ChallengeCategory category) {
        ChallengeCategoryJpaEntity entity = toEntity(category);
        ChallengeCategoryJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Optional<ChallengeCategory> findById(Long id) {
        return jpaRepository.findById(id)
            .map(this::toDomain);
    }
    
    @Override
    public List<ChallengeCategory> findAllActiveOrderBySortOrder() {
        return jpaRepository.findAllActiveOrderBySortOrder()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ChallengeCategory> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
            .map(this::toDomain);
    }
    
    @Override
    public Optional<ChallengeCategory> findByName(String name) {
        return jpaRepository.findByName(name)
            .map(this::toDomain);
    }
    
    @Override
    public void delete(ChallengeCategory category) {
        ChallengeCategoryJpaEntity entity = toEntity(category);
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
    
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
    
    // 엔티티 변환 메서드들
    private ChallengeCategory toDomain(ChallengeCategoryJpaEntity entity) {
        return ChallengeCategory.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .colorCode(entity.getColor())
            .iconName(entity.getIcon())
            .displayOrder(entity.getSortOrder())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    private ChallengeCategoryJpaEntity toEntity(ChallengeCategory domain) {
        ChallengeCategoryJpaEntity entity = new ChallengeCategoryJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setColor(domain.getColorCode());
        entity.setIcon(domain.getIconName());
        entity.setSortOrder(domain.getDisplayOrder());
        entity.setIsActive(domain.isActive());
        return entity;
    }
}