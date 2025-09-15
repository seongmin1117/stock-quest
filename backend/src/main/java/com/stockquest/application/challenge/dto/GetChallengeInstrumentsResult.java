package com.stockquest.application.challenge.dto;

import com.stockquest.domain.challenge.ChallengeInstrument;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 챌린지 상품 조회 결과
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GetChallengeInstrumentsResult {

    private Long challengeId;
    private List<ChallengeInstrumentInfo> instruments;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ChallengeInstrumentInfo {
        private Long id;
        private String instrumentKey;
        private String actualTicker;
        private String hiddenName;
        private String actualName;
        private ChallengeInstrument.InstrumentType type;

        public static ChallengeInstrumentInfo from(ChallengeInstrument instrument) {
            return ChallengeInstrumentInfo.builder()
                    .id(instrument.getId())
                    .instrumentKey(instrument.getInstrumentKey())
                    .actualTicker(instrument.getActualTicker())
                    .hiddenName(instrument.getHiddenName())
                    .actualName(instrument.getActualName())
                    .type(instrument.getType())
                    .build();
        }
    }

    public static GetChallengeInstrumentsResult of(Long challengeId, List<ChallengeInstrument> instruments) {
        List<ChallengeInstrumentInfo> instrumentInfos = instruments.stream()
                .map(ChallengeInstrumentInfo::from)
                .toList();

        return GetChallengeInstrumentsResult.builder()
                .challengeId(challengeId)
                .instruments(instrumentInfos)
                .build();
    }
}