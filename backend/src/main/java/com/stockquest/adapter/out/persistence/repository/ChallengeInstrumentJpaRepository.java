package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.ChallengeInstrumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 상품 JPA 리포지토리
 */
@Repository
public interface ChallengeInstrumentJpaRepository extends JpaRepository<ChallengeInstrumentJpaEntity, Long> {

    /**
     * 챌린지 ID로 상품 목록 조회
     */
    List<ChallengeInstrumentJpaEntity> findByChallengeId(Long challengeId);

    /**
     * 챌린지 ID와 상품 키로 상품 조회
     */
    Optional<ChallengeInstrumentJpaEntity> findByChallengeIdAndInstrumentKey(Long challengeId, String instrumentKey);

    /**
     * 실제 티커로 상품 조회
     */
    List<ChallengeInstrumentJpaEntity> findByActualTicker(String actualTicker);

    /**
     * 챌린지 ID와 상품 타입으로 상품 목록 조회
     */
    List<ChallengeInstrumentJpaEntity> findByChallengeIdAndType(Long challengeId, String type);
}