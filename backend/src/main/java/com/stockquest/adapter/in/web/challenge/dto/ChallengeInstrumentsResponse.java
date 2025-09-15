package com.stockquest.adapter.in.web.challenge.dto;

import com.stockquest.application.challenge.dto.GetChallengeInstrumentsResult;
import com.stockquest.domain.challenge.ChallengeInstrument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 챌린지 상품 목록 응답
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "챌린지 상품 목록 응답")
public class ChallengeInstrumentsResponse {

    @Schema(description = "챌린지 ID", example = "1")
    private Long challengeId;

    @Schema(description = "상품 목록")
    private List<InstrumentInfo> instruments;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Schema(description = "챌린지 상품 정보")
    public static class InstrumentInfo {

        @Schema(description = "상품 ID", example = "1")
        private Long id;

        @Schema(description = "상품 키 (A, B, C 등)", example = "A")
        private String instrumentKey;

        @Schema(description = "실제 티커", example = "AAPL")
        private String actualTicker;

        @Schema(description = "숨겨진 표시명", example = "회사 A")
        private String hiddenName;

        @Schema(description = "실제 회사명", example = "Apple Inc.")
        private String actualName;

        @Schema(description = "상품 유형")
        private String type;

        public static InstrumentInfo from(GetChallengeInstrumentsResult.ChallengeInstrumentInfo info) {
            return InstrumentInfo.builder()
                    .id(info.getId())
                    .instrumentKey(info.getInstrumentKey())
                    .actualTicker(info.getActualTicker())
                    .hiddenName(info.getHiddenName())
                    .actualName(info.getActualName())
                    .type(info.getType().name())
                    .build();
        }
    }

    public static ChallengeInstrumentsResponse from(GetChallengeInstrumentsResult result) {
        List<InstrumentInfo> instruments = result.getInstruments().stream()
                .map(InstrumentInfo::from)
                .toList();

        return ChallengeInstrumentsResponse.builder()
                .challengeId(result.getChallengeId())
                .instruments(instruments)
                .build();
    }
}