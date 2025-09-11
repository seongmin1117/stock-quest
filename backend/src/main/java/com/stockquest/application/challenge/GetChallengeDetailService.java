package com.stockquest.application.challenge;

import com.stockquest.application.challenge.dto.GetChallengeDetailQuery;
import com.stockquest.application.challenge.dto.GetChallengeDetailResult;
import com.stockquest.application.challenge.port.in.GetChallengeDetailUseCase;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 챌린지 상세 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetChallengeDetailService implements GetChallengeDetailUseCase {
    
    private final ChallengeRepository challengeRepository;
    private final ChallengeSessionRepository sessionRepository;
    
    @Override
    public GetChallengeDetailResult getChallengeDetail(GetChallengeDetailQuery query) {
        // 챌린지 조회
        Challenge challenge = challengeRepository.findById(query.challengeId())
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + query.challengeId()));
        
        // 사용자의 활성 세션 조회 (있을 경우)
        ChallengeSession userSession = sessionRepository
                .findByUserIdAndChallengeIdAndStatus(query.userId(), query.challengeId(), SessionStatus.ACTIVE)
                .orElse(null);
        
        return new GetChallengeDetailResult(challenge, userSession);
    }
}