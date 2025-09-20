package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.LeaderboardJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 리더보드 JPA Repository
 */
@Repository
public interface LeaderboardJpaRepository extends JpaRepository<LeaderboardJpaEntity, Long> {
    
    /**
     * 챌린지별 리더보드 조회 (수익률 높은 순)
     */
    List<LeaderboardJpaEntity> findByChallengeIdOrderByReturnPercentageDesc(Long challengeId);
    
    /**
     * 특정 챌린지의 특정 사용자 엔트리 조회
     */
    Optional<LeaderboardJpaEntity> findByChallengeIdAndUserId(Long challengeId, Long userId);
    
    /**
     * 챌린지별 상위 N명 조회
     */
    @Query("SELECT l FROM LeaderboardJpaEntity l WHERE l.challengeId = :challengeId ORDER BY l.returnPercentage DESC LIMIT :limit")
    List<LeaderboardJpaEntity> findTopNByChallengeId(@Param("challengeId") Long challengeId, @Param("limit") int limit);
    
    /**
     * 챌린지 완료된 참가자 수 조회
     */
    long countByChallengeId(Long challengeId);
    
    /**
     * 기존 엔트리 삭제 (재계산용)
     */
    void deleteByChallengeId(Long challengeId);
}