package com.stockquest.application.challenge;

import com.stockquest.application.challenge.dto.GetChallengeListQuery;
import com.stockquest.application.challenge.dto.GetChallengeListResult;
import com.stockquest.application.challenge.port.in.GetChallengeListUseCase;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 챌린지 목록 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetChallengeListService implements GetChallengeListUseCase {
    
    private final ChallengeRepository challengeRepository;
    
    @Override
    public GetChallengeListResult getChallengeList(GetChallengeListQuery query) {
        // 활성 상태의 챌린지만 조회
        List<Challenge> challenges = challengeRepository.findByStatus(ChallengeStatus.ACTIVE);
        
        // 페이징 처리 (간단한 구현)
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), challenges.size());
        List<Challenge> pagedChallenges = challenges.subList(start, end);
        
        return new GetChallengeListResult(
                pagedChallenges,
                challenges.size(),
                query.page(),
                query.size()
        );
    }
}