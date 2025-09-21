package com.stockquest.domain.challenge.port;

import com.stockquest.domain.challenge.ChallengeCategory;
import com.stockquest.domain.common.Page;
import com.stockquest.domain.common.PageRequest;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 카테고리 저장소 포트
 */
public interface ChallengeCategoryRepository {
    
    /**
     * 카테고리 저장
     */
    ChallengeCategory save(ChallengeCategory category);
    
    /**
     * ID로 카테고리 조회
     */
    Optional<ChallengeCategory> findById(Long id);
    
    /**
     * 모든 활성 카테고리 조회 (정렬 순서대로)
     */
    List<ChallengeCategory> findAllActiveOrderBySortOrder();
    
    /**
     * 페이지네이션된 카테고리 목록 조회
     */
    Page<ChallengeCategory> findAll(PageRequest pageRequest);
    
    /**
     * 이름으로 카테고리 조회
     */
    Optional<ChallengeCategory> findByName(String name);
    
    /**
     * 카테고리 삭제
     */
    void delete(ChallengeCategory category);
    
    /**
     * ID로 카테고리 삭제
     */
    void deleteById(Long id);
    
    /**
     * 카테고리 존재 여부 확인
     */
    boolean existsById(Long id);
    
    /**
     * 이름으로 카테고리 존재 여부 확인
     */
    boolean existsByName(String name);
}