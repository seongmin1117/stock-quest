package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.company.CompanyCategoryJpaRepository;
import com.stockquest.adapter.out.persistence.entity.CompanyCategoryJpaEntity;
import com.stockquest.domain.company.CompanyCategory;
import com.stockquest.domain.company.port.CompanyCategoryRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 회사 카테고리 저장소 어댑터
 * Domain CompanyCategoryRepositoryPort 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 * 헥사고날 아키텍처 준수 - 도메인과 JPA 엔티티 간 변환 담당
 */
@Component
public class CompanyCategoryRepositoryAdapter implements CompanyCategoryRepositoryPort {

    private final CompanyCategoryJpaRepository jpaRepository;

    public CompanyCategoryRepositoryAdapter(CompanyCategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CompanyCategory> findByCategoryId(String categoryId) {
        return jpaRepository.findByCategoryId(categoryId)
                .map(CompanyCategoryJpaEntity::toDomain);
    }

    @Override
    public List<CompanyCategory> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderBySortOrder()
                .stream()
                .map(CompanyCategoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<CompanyCategory> findAll() {
        return jpaRepository.findAllByOrderBySortOrder()
                .stream()
                .map(CompanyCategoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<CompanyCategory> findByNameKr(String nameKr) {
        return jpaRepository.findByNameKrContainingIgnoreCase(nameKr)
                .stream()
                .map(CompanyCategoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<CompanyCategory> findByNameEn(String nameEn) {
        return jpaRepository.findByNameEnContainingIgnoreCase(nameEn)
                .stream()
                .map(CompanyCategoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public CompanyCategory save(CompanyCategory category) {
        CompanyCategoryJpaEntity jpaEntity;

        if (category.getId() == null) {
            // 새로운 엔티티 생성
            jpaEntity = CompanyCategoryJpaEntity.fromDomainForCreate(category);
        } else {
            // 기존 엔티티 업데이트
            Optional<CompanyCategoryJpaEntity> existingEntity = jpaRepository.findById(category.getId());
            if (existingEntity.isPresent()) {
                jpaEntity = existingEntity.get().updateFromDomain(category);
            } else {
                jpaEntity = CompanyCategoryJpaEntity.fromDomain(category);
            }
        }

        CompanyCategoryJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }

    @Override
    public void delete(CompanyCategory category) {
        if (category.getId() != null) {
            jpaRepository.deleteById(category.getId());
        }
    }

    @Override
    public boolean existsByCategoryId(String categoryId) {
        return jpaRepository.existsByCategoryId(categoryId);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countActive() {
        return jpaRepository.countByIsActiveTrue();
    }

    // Additional methods using new JPA repository features

    /**
     * 카테고리 검색
     */
    public List<CompanyCategory> searchCategories(String query) {
        return jpaRepository.searchCategories(query)
                .stream()
                .map(CompanyCategoryJpaEntity::toDomain)
                .toList();
    }

    /**
     * 정렬 순서 범위로 카테고리 조회
     */
    public List<CompanyCategory> findBySortOrderRange(int minOrder, int maxOrder) {
        return jpaRepository.findBySortOrderRange(minOrder, maxOrder)
                .stream()
                .map(CompanyCategoryJpaEntity::toDomain)
                .toList();
    }

    /**
     * 설명에 특정 텍스트를 포함하는 카테고리 조회
     */
    public List<CompanyCategory> findByDescriptionContaining(String text) {
        return jpaRepository.findByDescriptionContaining(text)
                .stream()
                .map(CompanyCategoryJpaEntity::toDomain)
                .toList();
    }

    /**
     * 활성 카테고리 ID로 조회
     */
    public Optional<CompanyCategory> findActiveByCategoryId(String categoryId) {
        return jpaRepository.findActiveByCategoryId(categoryId)
                .map(CompanyCategoryJpaEntity::toDomain);
    }
}