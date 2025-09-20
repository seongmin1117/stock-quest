package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.ChallengeCategoryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 카테고리 JPA Repository
 */
@Repository
public interface ChallengeCategoryJpaRepository extends JpaRepository<ChallengeCategoryJpaEntity, Long> {
    
    /**
     * 모든 활성 카테고리를 정렬 순서대로 조회
     */
    @Query("SELECT c FROM ChallengeCategoryJpaEntity c WHERE c.isActive = true ORDER BY c.sortOrder")
    List<ChallengeCategoryJpaEntity> findAllActiveOrderBySortOrder();
    
    /**
     * 이름으로 카테고리 조회
     */
    Optional<ChallengeCategoryJpaEntity> findByName(String name);
    
    /**
     * 이름으로 카테고리 존재 여부 확인
     */
    boolean existsByName(String name);
    
    /**
     * 페이지네이션으로 카테고리 조회
     */
    Page<ChallengeCategoryJpaEntity> findAll(Pageable pageable);
}