package com.stockquest.application.challenge;

import com.stockquest.application.challenge.dto.GetChallengeDetailQuery;
import com.stockquest.application.challenge.dto.GetChallengeDetailResult;
import com.stockquest.application.challenge.port.in.GetChallengeDetailUseCase;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeInstrument;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 챌린지 상세 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetChallengeDetailService implements GetChallengeDetailUseCase {

    private final ChallengeRepository challengeRepository;
    private final ChallengeSessionRepository sessionRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public GetChallengeDetailResult getChallengeDetail(GetChallengeDetailQuery query) {
        // 챌린지 조회
        Challenge challenge = challengeRepository.findById(query.challengeId())
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + query.challengeId()));

        // 챌린지 상품 조회
        List<ChallengeInstrument> instruments = jdbcTemplate.query(
            "SELECT instrument_key, actual_ticker, hidden_name, actual_name, type " +
            "FROM challenge_instrument WHERE challenge_id = ?",
            new Object[]{query.challengeId()},
            (rs, rowNum) -> ChallengeInstrument.builder()
                    .challengeId(query.challengeId())
                    .instrumentKey(rs.getString("instrument_key"))
                    .actualTicker(rs.getString("actual_ticker"))
                    .hiddenName(rs.getString("hidden_name"))
                    .actualName(rs.getString("actual_name"))
                    .type(ChallengeInstrument.InstrumentType.valueOf(rs.getString("type")))
                    .build()
        );

        // Challenge 객체에 instruments 설정 (toBuilder 패턴 사용)
        Challenge challengeWithInstruments = challenge.toBuilder()
                .instruments(instruments)
                .build();

        // 사용자의 활성 세션 조회 (있을 경우)
        ChallengeSession userSession = sessionRepository
                .findByUserIdAndChallengeIdAndStatus(query.userId(), query.challengeId(), SessionStatus.ACTIVE)
                .orElse(null);

        return new GetChallengeDetailResult(challengeWithInstruments, userSession);
    }
}