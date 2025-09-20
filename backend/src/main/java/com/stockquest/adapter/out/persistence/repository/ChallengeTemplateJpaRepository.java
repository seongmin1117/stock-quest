package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.ChallengeTemplateJpaEntity;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.ChallengeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 챌린지 템플릿 JPA Repository
 */
@Repository
public interface ChallengeTemplateJpaRepository extends JpaRepository<ChallengeTemplateJpaEntity, Long> {
    
    /**
     * 모든 활성 템플릿 조회
     */
    @Query("SELECT t FROM ChallengeTemplateJpaEntity t WHERE t.isActive = true ORDER BY t.createdAt DESC")
    List<ChallengeTemplateJpaEntity> findAllActive();
    
    /**
     * 카테고리별 템플릿 목록 조회
     */
    @Query("SELECT t FROM ChallengeTemplateJpaEntity t WHERE t.categoryId = :categoryId AND t.isActive = true ORDER BY t.createdAt DESC")
    List<ChallengeTemplateJpaEntity> findByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 템플릿 유형별 조회
     */
    @Query("SELECT t FROM ChallengeTemplateJpaEntity t WHERE t.templateType = :templateType AND t.isActive = true ORDER BY t.createdAt DESC")
    List<ChallengeTemplateJpaEntity> findByTemplateType(@Param("templateType") ChallengeType templateType);
    
    /**
     * 난이도별 조회
     */
    @Query("SELECT t FROM ChallengeTemplateJpaEntity t WHERE t.difficulty = :difficulty AND t.isActive = true ORDER BY t.createdAt DESC")
    List<ChallengeTemplateJpaEntity> findByDifficulty(@Param("difficulty") ChallengeDifficulty difficulty);
    
    /**
     * 이름으로 템플릿 검색
     */
    @Query("SELECT t FROM ChallengeTemplateJpaEntity t WHERE t.name LIKE %:name% AND t.isActive = true ORDER BY t.createdAt DESC")
    List<ChallengeTemplateJpaEntity> findByNameContaining(@Param("name") String name);
    
    /**
     * 생성자별 템플릿 조회
     */
    @Query("SELECT t FROM ChallengeTemplateJpaEntity t WHERE t.createdBy = :userId AND t.isActive = true ORDER BY t.createdAt DESC")
    List<ChallengeTemplateJpaEntity> findByCreatedBy(@Param("userId") Long userId);
    
    /**
     * 태그로 템플릿 검색 (JSON_CONTAINS 사용)
     */
    @Query(value = "SELECT * FROM challenge_template t WHERE JSON_CONTAINS(t.tags, JSON_QUOTE(:tag)) AND t.is_active = true ORDER BY t.created_at DESC", nativeQuery = true)
    List<ChallengeTemplateJpaEntity> findByTagsContaining(@Param("tag") String tag);
    
    /**
     * 페이지네이션으로 템플릿 조회
     */
    @Query("SELECT t FROM ChallengeTemplateJpaEntity t WHERE t.isActive = true")
    Page<ChallengeTemplateJpaEntity> findAllActive(Pageable pageable);
}