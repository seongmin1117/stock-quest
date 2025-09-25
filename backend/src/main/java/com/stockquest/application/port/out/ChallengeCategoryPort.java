package com.stockquest.application.port.out;

import com.stockquest.domain.challenge.ChallengeCategory;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 카테고리 데이터 액세스를 위한 포트 인터페이스
 */
public interface ChallengeCategoryPort {
    
    /**
     * 챌린지 카테고리 저장
     */
    ChallengeCategory save(ChallengeCategory category);
    
    /**
     * ID로 카테고리 조회
     */
    Optional<ChallengeCategory> findById(Long id);
    
    /**
     * 활성 상태의 모든 카테고리 조회
     */
    List<ChallengeCategory> findAllActive();
    
    /**
     * 모든 카테고리 조회
     */
    List<ChallengeCategory> findAll();
    
    /**
     * 이름으로 카테고리 조회
     */
    Optional<ChallengeCategory> findByName(String name);
    
    /**
     * 카테고리 삭제
     */
    void deleteById(Long id);
    
    /**
     * 상위 카테고리로 하위 카테고리 조회
     */
    List<ChallengeCategory> findByParentId(Long parentId);
}