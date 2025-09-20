package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.company.CompanyJpaRepository;
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
        return jpaRepository.findBySymbol(symbol);
    }

    @Override
    public List<Company> findByCategory(String categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Company> findByCategories(List<String> categoryIds) {
        return jpaRepository.findByCategoryIds(categoryIds);
    }

    @Override
    public List<Company> searchByName(String name) {
        return jpaRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Company> findTopByPopularity(int limit) {
        if (limit > 50) {
            limit = 50; // Repository method is limited to top 50
        }
        List<Company> allTop = jpaRepository.findTop50ByIsActiveTrueOrderByPopularityScoreDesc();
        return allTop.stream().limit(limit).toList();
    }

    @Override
    public List<Company> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderByPopularityScoreDesc();
    }

    @Override
    public Company save(Company company) {
        return jpaRepository.save(company);
    }

    @Override
    public void delete(Company company) {
        jpaRepository.delete(company);
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
        return jpaRepository.findAllActiveOrderByPopularity();
    }

    @Override
    public void updateMarketCap(String symbol, Long marketCap) {
        Optional<Company> company = jpaRepository.findBySymbol(symbol);
        if (company.isPresent()) {
            Company existingCompany = company.get();
            existingCompany.updateMarketCap(marketCap, null);
            jpaRepository.save(existingCompany);
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
}