package com.stockquest.domain.challenge.port;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;
import java.util.List;
import java.util.Optional;

/**
 * 챌린지 저장소 포트 (출력 포트)
 * 도메인에서 정의하는 챌린지 데이터 접근 인터페이스
 */
public interface ChallengeRepository {
    
    /**
     * 챌린지 저장
     */
    Challenge save(Challenge challenge);
    
    /**
     * ID로 챌린지 조회
     */
    Optional<Challenge> findById(Long id);
    
    /**
     * 상태별 챌린지 목록 조회
     */
    List<Challenge> findByStatus(ChallengeStatus status);
    
    /**
     * 모든 챌린지 조회 (페이징)
     */
    List<Challenge> findAll(int page, int size);
    
    /**
     * 활성 챌린지 목록 조회
     */
    List<Challenge> findActiveChallenges();
}