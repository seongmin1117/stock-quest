package com.stockquest.domain.challenge.port;

import com.stockquest.domain.challenge.ChallengeTemplate;
import com.stockquest.domain.challenge.ChallengeType;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 템플릿 저장소 포트
 */
public interface ChallengeTemplateRepository {
    
    /**
     * 템플릿 저장
     */
    ChallengeTemplate save(ChallengeTemplate template);
    
    /**
     * ID로 템플릿 조회
     */
    Optional<ChallengeTemplate> findById(Long id);
    
    /**
     * 활성 템플릿 목록 조회
     */
    List<ChallengeTemplate> findAllActive();
    
    /**
     * 페이지네이션된 템플릿 목록 조회
     */
    Page<ChallengeTemplate> findAll(Pageable pageable);
    
    /**
     * 카테고리별 템플릿 목록 조회
     */
    List<ChallengeTemplate> findByCategoryId(Long categoryId);
    
    /**
     * 템플릿 유형별 조회
     */
    List<ChallengeTemplate> findByTemplateType(ChallengeType templateType);
    
    /**
     * 난이도별 조회
     */
    List<ChallengeTemplate> findByDifficulty(ChallengeDifficulty difficulty);
    
    /**
     * 태그로 템플릿 검색
     */
    List<ChallengeTemplate> findByTagsContaining(String tag);
    
    /**
     * 이름으로 템플릿 검색
     */
    List<ChallengeTemplate> findByNameContaining(String name);
    
    /**
     * 생성자별 템플릿 조회
     */
    List<ChallengeTemplate> findByCreatedBy(Long userId);
    
    /**
     * 템플릿 삭제
     */
    void delete(ChallengeTemplate template);
    
    /**
     * ID로 템플릿 삭제
     */
    void deleteById(Long id);
    
    /**
     * 템플릿 존재 여부 확인
     */
    boolean existsById(Long id);
}