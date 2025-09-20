package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.company.CompanyCategoryJpaRepository;
import com.stockquest.domain.company.CompanyCategory;
import com.stockquest.domain.company.port.CompanyCategoryRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 회사 카테고리 저장소 어댑터
 * Domain CompanyCategoryRepositoryPort 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 */
@Component
public class CompanyCategoryRepositoryAdapter implements CompanyCategoryRepositoryPort {

    private final CompanyCategoryJpaRepository jpaRepository;

    public CompanyCategoryRepositoryAdapter(CompanyCategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CompanyCategory> findByCategoryId(String categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<CompanyCategory> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderBySortOrder();
    }

    @Override
    public List<CompanyCategory> findAll() {
        return jpaRepository.findAllByOrderBySortOrder();
    }

    @Override
    public List<CompanyCategory> findByNameKr(String nameKr) {
        return jpaRepository.findByNameKrContainingIgnoreCase(nameKr);
    }

    @Override
    public List<CompanyCategory> findByNameEn(String nameEn) {
        return jpaRepository.findByNameEnContainingIgnoreCase(nameEn);
    }

    @Override
    public CompanyCategory save(CompanyCategory category) {
        return jpaRepository.save(category);
    }

    @Override
    public void delete(CompanyCategory category) {
        jpaRepository.delete(category);
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
}