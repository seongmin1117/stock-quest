package com.stockquest.application.port.out;

import com.stockquest.application.challenge.dto.ChallengePage;
import com.stockquest.application.challenge.dto.ChallengeSearchCriteria;
import com.stockquest.domain.challenge.Challenge;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 데이터 액세스를 위한 포트 인터페이스
 */
public interface ChallengePort {
    
    /**
     * 챌린지 저장
     */
    Challenge save(Challenge challenge);
    
    /**
     * ID로 챌린지 조회
     */
    Optional<Challenge> findById(Long id);
    
    /**
     * 검색 조건으로 챌린지 목록 조회 (페이징)
     */
    ChallengePage findByCriteria(ChallengeSearchCriteria criteria);
    
    /**
     * 카테고리별 챌린지 조회
     */
    List<Challenge> findByCategoryId(Long categoryId);
    
    /**
     * 템플릿별 챌린지 조회
     */
    List<Challenge> findByTemplateId(Long templateId);
    
    /**
     * 인기 챌린지 조회
     */
    List<Challenge> findPopularChallenges(int limit);
    
    /**
     * 피처드 챌린지 조회
     */
    List<Challenge> findFeaturedChallenges();
    
    /**
     * 챌린지 삭제
     */
    void deleteById(Long id);
    
    /**
     * 모든 챌린지 조회
     */
    List<Challenge> findAll();
    
    /**
     * 상태별 챌린지 조회
     */
    List<Challenge> findByStatus(String status);
}