package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.ChallengeJpaEntity;
import com.stockquest.domain.challenge.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 챌린지 JPA 저장소
 */
@Repository
public interface ChallengeJpaRepository extends JpaRepository<ChallengeJpaEntity, Long> {
    
    List<ChallengeJpaEntity> findByStatus(ChallengeStatus status);
    
    @Query("SELECT c FROM ChallengeJpaEntity c WHERE c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    List<ChallengeJpaEntity> findActiveChallenges();
}