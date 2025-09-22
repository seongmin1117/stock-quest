package com.stockquest.application.challenge;

import com.stockquest.application.challenge.dto.GetChallengeListQuery;
import com.stockquest.application.challenge.dto.GetChallengeListResult;
import com.stockquest.application.challenge.port.in.GetChallengeListUseCase;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 챌린지 목록 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetChallengeListService implements GetChallengeListUseCase {

    private final ChallengeRepository challengeRepository;
    private final ChallengeSessionRepository challengeSessionRepository;
    
    @Override
    public GetChallengeListResult getChallengeList(GetChallengeListQuery query) {
        // 활성 상태의 챌린지만 조회
        List<Challenge> activeChallenges = challengeRepository.findByStatus(ChallengeStatus.ACTIVE);

        // 사용자별 필터링 로직 적용
        List<Challenge> availableChallenges = filterAvailableChallengesForUser(activeChallenges, query.userId());

        // 페이징 처리 (간단한 구현)
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), availableChallenges.size());
        List<Challenge> pagedChallenges = availableChallenges.subList(start, end);

        return new GetChallengeListResult(
                pagedChallenges,
                availableChallenges.size(),
                query.page(),
                query.size()
        );
    }

    /**
     * 사용자에게 표시할 수 있는 챌린지 필터링
     * 비인증 사용자: 모든 활성 챌린지
     * 인증 사용자: 완료하지 않은 활성 챌린지만
     */
    private List<Challenge> filterAvailableChallengesForUser(List<Challenge> activeChallenges, Long userId) {
        // 비인증 사용자인 경우 모든 활성 챌린지 반환
        if (userId == null) {
            return activeChallenges;
        }

        // 사용자가 완료한 챌린지 ID 조회
        Set<Long> completedChallengeIds = challengeSessionRepository
                .findByUserIdAndStatus(userId, SessionStatus.COMPLETED)
                .stream()
                .map(session -> session.getChallengeId())
                .collect(Collectors.toSet());

        // 완료하지 않은 챌린지만 필터링
        return activeChallenges.stream()
                .filter(challenge -> !completedChallengeIds.contains(challenge.getId()))
                .collect(Collectors.toList());
    }
}