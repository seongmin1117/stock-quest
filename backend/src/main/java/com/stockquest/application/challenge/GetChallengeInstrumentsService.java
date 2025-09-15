package com.stockquest.application.challenge;

import com.stockquest.application.challenge.dto.GetChallengeInstrumentsQuery;
import com.stockquest.application.challenge.dto.GetChallengeInstrumentsResult;
import com.stockquest.application.challenge.port.in.GetChallengeInstrumentsUseCase;
import com.stockquest.domain.challenge.ChallengeInstrument;
import com.stockquest.domain.challenge.port.ChallengeInstrumentRepository;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 챌린지 상품 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetChallengeInstrumentsService implements GetChallengeInstrumentsUseCase {

    private final ChallengeRepository challengeRepository;
    private final ChallengeInstrumentRepository challengeInstrumentRepository;

    @Override
    public GetChallengeInstrumentsResult getChallengeInstruments(GetChallengeInstrumentsQuery query) {
        log.debug("챌린지 상품 조회 시작: challengeId={}", query.challengeId());

        // 챌린지 존재 확인
        if (challengeRepository.findById(query.challengeId()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 챌린지입니다: " + query.challengeId());
        }

        // 챌린지에 속한 상품 목록 조회
        List<ChallengeInstrument> instruments = challengeInstrumentRepository.findByChallengeId(query.challengeId());

        log.debug("챌린지 상품 조회 완료: challengeId={}, instrumentCount={}", query.challengeId(), instruments.size());

        return GetChallengeInstrumentsResult.of(query.challengeId(), instruments);
    }
}