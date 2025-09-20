package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.CommunityPostJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 커뮤니티 게시글 JPA Repository
 */
@Repository
public interface CommunityPostJpaRepository extends JpaRepository<CommunityPostJpaEntity, Long> {
    
    /**
     * 챌린지별 게시글 목록 조회 (최신순)
     */
    List<CommunityPostJpaEntity> findByChallengeIdOrderByCreatedAtDesc(Long challengeId);
    
    /**
     * 작성자별 게시글 목록 조회
     */
    List<CommunityPostJpaEntity> findByAuthorId(Long authorId);
    
    /**
     * 챌린지별 게시글 개수 조회
     */
    long countByChallengeId(Long challengeId);
}