package com.stockquest.application.challenge;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.application.port.out.ChallengePort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 챌린지 조회 서비스
 * 캐싱을 활용한 챌린지 목록 및 상세 정보 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetChallengesService {
    
    private final ChallengePort challengePort;
    
    /**
     * 활성 챌린지 목록 조회
     */
    @Cacheable(value = "activeChallenges", cacheManager = "cacheManager")
    public List<Challenge> getActiveChallenges() {
        // ChallengePort는 findByStatus 메소드가 없으므로 기본 구현
        // 실제로는 별도의 쿼리 메소드가 필요함
        return challengePort.findFeaturedChallenges(); // 임시로 featured 사용
    }
    
    /**
     * 활성 챌린지 ID 목록 조회
     */
    @Cacheable(value = "activeChallengeIds", cacheManager = "cacheManager")
    public List<Long> getActiveChallengeIds() {
        return getActiveChallenges().stream()
                .map(Challenge::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * 인기 챌린지 ID 목록 조회 (참여자 수 기준)
     */
    @Cacheable(value = "popularChallengeIds", cacheManager = "cacheManager")
    public List<Long> getPopularChallengeIds(int limit) {
        return challengePort.findPopularChallenges(limit).stream()
                .map(Challenge::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * 챌린지 상세 정보 조회
     */
    @Cacheable(value = "challengeDetail", key = "#challengeId", cacheManager = "cacheManager")
    public Challenge getChallengeDetail(Long challengeId) {
        return challengePort.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));
    }
    
    /**
     * 활성 챌린지 수 조회
     */
    @Cacheable(value = "activeChallengeCount", cacheManager = "cacheManager")
    public long getActiveChallengeCount() {
        // 임시로 featured 챌린지 수를 반환 (size()는 int이므로 long으로 명시적 캐스팅)
        return (long) challengePort.findFeaturedChallenges().size();
    }
}