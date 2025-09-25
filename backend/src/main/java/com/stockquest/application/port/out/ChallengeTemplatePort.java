package com.stockquest.application.port.out;

import com.stockquest.domain.challenge.ChallengeTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 템플릿 데이터 액세스를 위한 포트 인터페이스
 */
public interface ChallengeTemplatePort {
    
    /**
     * 챌린지 템플릿 저장
     */
    ChallengeTemplate save(ChallengeTemplate template);
    
    /**
     * ID로 템플릿 조회
     */
    Optional<ChallengeTemplate> findById(Long id);
    
    /**
     * 활성 상태의 모든 템플릿 조회
     */
    List<ChallengeTemplate> findAllActive();
    
    /**
     * 모든 템플릿 조회
     */
    List<ChallengeTemplate> findAll();
    
    /**
     * 카테고리별 템플릿 조회
     */
    List<ChallengeTemplate> findByCategoryId(Long categoryId);
    
    /**
     * 인기 템플릿 조회 (사용횟수 기준)
     */
    List<ChallengeTemplate> findPopularTemplates(int limit);
    
    /**
     * 난이도별 템플릿 조회
     */
    List<ChallengeTemplate> findByDifficulty(String difficulty);
    
    /**
     * 템플릿 삭제
     */
    void deleteById(Long id);
    
    /**
     * 이름으로 템플릿 검색
     */
    List<ChallengeTemplate> findByNameContaining(String name);
}