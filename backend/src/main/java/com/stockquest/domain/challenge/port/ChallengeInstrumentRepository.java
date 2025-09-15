package com.stockquest.domain.challenge.port;

import com.stockquest.domain.challenge.ChallengeInstrument;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 상품 리포지토리 포트
 */
public interface ChallengeInstrumentRepository {

    /**
     * 챌린지 ID로 상품 목록 조회
     */
    List<ChallengeInstrument> findByChallengeId(Long challengeId);

    /**
     * 챌린지 ID와 상품 키로 상품 조회
     */
    Optional<ChallengeInstrument> findByChallengeIdAndInstrumentKey(Long challengeId, String instrumentKey);

    /**
     * 상품 저장
     */
    ChallengeInstrument save(ChallengeInstrument instrument);

    /**
     * 상품 목록 저장
     */
    List<ChallengeInstrument> saveAll(List<ChallengeInstrument> instruments);

    /**
     * ID로 상품 조회
     */
    Optional<ChallengeInstrument> findById(Long id);

    /**
     * 상품 삭제
     */
    void delete(ChallengeInstrument instrument);
}