package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.company.CompanyJpaRepository;
import com.stockquest.adapter.out.persistence.entity.CompanyJpaEntity;
import com.stockquest.application.company.CompanySyncLog;
import com.stockquest.application.company.CompanySyncBatchLog;
import com.stockquest.domain.company.Company;
import com.stockquest.domain.company.port.CompanyRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 회사 저장소 어댑터
 * Domain CompanyRepositoryPort 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 * 헥사고날 아키텍처 준수 - 도메인과 JPA 엔티티 간 변환 담당
 */
@Slf4j
@Component
public class CompanyRepositoryAdapter implements CompanyRepositoryPort {

    private final CompanyJpaRepository jpaRepository;

    public CompanyRepositoryAdapter(CompanyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Company> findBySymbol(String symbol) {
        return jpaRepository.findBySymbol(symbol)
                .map(CompanyJpaEntity::toDomain);
    }

    @Override
    public List<Company> findByCategory(String categoryId) {
        return jpaRepository.findByCategoryId(categoryId)
                .stream()
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Company> findByCategories(List<String> categoryIds) {
        return jpaRepository.findByCategoryIds(categoryIds)
                .stream()
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Company> searchByName(String name) {
        return jpaRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Company> findTopByPopularity(int limit) {
        if (limit > 50) {
            limit = 50; // Repository method is limited to top 50
        }
        List<CompanyJpaEntity> allTop = jpaRepository.findTop50ByIsActiveTrueOrderByPopularityScoreDesc();
        return allTop.stream()
                .limit(limit)
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Company> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderByPopularityScoreDesc()
                .stream()
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Company save(Company company) {
        CompanyJpaEntity jpaEntity;

        if (company.getId() == null) {
            // 새로운 엔티티 생성
            jpaEntity = CompanyJpaEntity.fromDomainForCreate(company);
        } else {
            // 기존 엔티티 업데이트
            Optional<CompanyJpaEntity> existingEntity = jpaRepository.findById(company.getId());
            if (existingEntity.isPresent()) {
                jpaEntity = existingEntity.get().updateFromDomain(company);
            } else {
                jpaEntity = CompanyJpaEntity.fromDomain(company);
            }
        }

        CompanyJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }

    @Override
    public void delete(Company company) {
        if (company.getId() != null) {
            jpaRepository.deleteById(company.getId());
        }
    }

    @Override
    public boolean existsBySymbol(String symbol) {
        return jpaRepository.existsBySymbol(symbol);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Company> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaRepository.findAllActiveOrderByPopularity()
                .stream()
                .skip((long) page * size)
                .limit(size)
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void updateMarketCap(String symbol, Long marketCap) {
        Optional<CompanyJpaEntity> companyEntity = jpaRepository.findBySymbol(symbol);
        if (companyEntity.isPresent()) {
            Company domainCompany = companyEntity.get().toDomain();
            Company updatedCompany = domainCompany.updateMarketCap(marketCap, null);

            CompanyJpaEntity updatedEntity = companyEntity.get().updateFromDomain(updatedCompany);
            jpaRepository.save(updatedEntity);
        }
    }

    @Override
    public void saveSyncLog(CompanySyncLog syncLog) {
        // TODO: CompanySyncLog persistence implementation needed
        // For now, just log the sync operation
        log.info("Company sync log - Symbol: {}, Success: {}, MarketCap: {} -> {}",
                syncLog.getSymbol(),
                syncLog.isSuccess(),
                syncLog.getPreviousMarketCap(),
                syncLog.getUpdatedMarketCap());
    }

    @Override
    public void saveSyncBatchLog(CompanySyncBatchLog batchLog) {
        // TODO: CompanySyncBatchLog persistence implementation needed
        // For now, just log the batch sync operation
        log.info("Company batch sync log - Total: {}, Success: {}, Failure: {}, Success Rate: {}%",
                batchLog.getTotalCompanies(),
                batchLog.getSuccessCount(),
                batchLog.getFailureCount(),
                batchLog.getSuccessRate());
    }

    // Additional methods using new JPA repository features

    /**
     * 거래소별 회사 조회
     */
    public List<Company> findByExchange(String exchange) {
        return jpaRepository.findByExchangeAndIsActiveTrueOrderByPopularityScoreDesc(exchange)
                .stream()
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    /**
     * 통화별 회사 조회
     */
    public List<Company> findByCurrency(String currency) {
        return jpaRepository.findByCurrencyAndIsActiveTrueOrderByPopularityScoreDesc(currency)
                .stream()
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    /**
     * 대형주 회사 조회 (시가총액 기준)
     */
    public List<Company> findLargeCapCompanies(Long minMarketCap) {
        return jpaRepository.findLargeCapCompanies(minMarketCap)
                .stream()
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }

    /**
     * 통합 검색 (이름, 심볼, 섹터)
     */
    public List<Company> searchCompanies(String query) {
        return jpaRepository.searchCompanies(query)
                .stream()
                .map(CompanyJpaEntity::toDomain)
                .toList();
    }
}