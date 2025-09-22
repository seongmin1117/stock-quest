package com.stockquest.application.order.validation;

import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 챌린지 데이터 검증 서비스
 * 챌린지 시작 전 필수 데이터 완성도를 검증 (주문 처리용)
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChallengeDataValidationService {

    private final ChallengeRepository challengeRepository;

    /**
     * 챌린지 데이터 검증
     */
    @Cacheable(value = "challengeValidation", key = "#challengeId")
    public ChallengeValidationResult validateChallenge(Long challengeId) {
        log.debug("Validating challenge data for challengeId={}", challengeId);

        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + challengeId));

        var builder = ChallengeValidationResult.builder()
            .challengeId(challengeId)
            .challengeTitle(challenge.getTitle())
            .valid(true);

        // 1. 상품 매핑 검증
        int instrumentCount = challenge.getInstruments().size();
        if (instrumentCount == 0) {
            builder.valid(false)
                .addIssue("상품 매핑이 없습니다. 거래할 수 있는 상품이 없습니다.");
        } else {
            builder.addInfo("상품 매핑: " + instrumentCount + "개 상품 사용 가능");
        }

        // 2. 챌린지 상태 검증
        if (challenge.getStatus() != com.stockquest.domain.challenge.ChallengeStatus.ACTIVE) {
            builder.valid(false)
                .addIssue("챌린지가 활성 상태가 아닙니다: " + challenge.getStatus());
        }

        // 3. 기간 검증
        if (challenge.getPeriodStart() == null || challenge.getPeriodEnd() == null) {
            builder.addWarning("챌린지 기간이 설정되지 않았습니다.");
        }

        // 4. 기본 설정 검증
        if (challenge.getInitialBalance() == null || challenge.getInitialBalance().doubleValue() <= 0) {
            builder.addWarning("초기 자금이 설정되지 않았거나 유효하지 않습니다.");
        }

        var result = builder.build();

        log.info("Challenge validation completed: challengeId={}, valid={}, issues={}, warnings={}",
                challengeId, result.isValid(), result.getIssues().size(), result.getWarnings().size());

        return result;
    }

    /**
     * 챌린지 데이터 검증 결과
     */
    public static class ChallengeValidationResult {
        private final Long challengeId;
        private final String challengeTitle;
        private final boolean valid;
        private final java.util.List<String> issues;
        private final java.util.List<String> warnings;
        private final java.util.List<String> info;

        private ChallengeValidationResult(Builder builder) {
            this.challengeId = builder.challengeId;
            this.challengeTitle = builder.challengeTitle;
            this.valid = builder.valid;
            this.issues = java.util.List.copyOf(builder.issues);
            this.warnings = java.util.List.copyOf(builder.warnings);
            this.info = java.util.List.copyOf(builder.info);
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public Long getChallengeId() { return challengeId; }
        public String getChallengeTitle() { return challengeTitle; }
        public boolean isValid() { return valid; }
        public java.util.List<String> getIssues() { return issues; }
        public java.util.List<String> getWarnings() { return warnings; }
        public java.util.List<String> getInfo() { return info; }

        public static class Builder {
            private Long challengeId;
            private String challengeTitle;
            private boolean valid = true;
            private final java.util.List<String> issues = new java.util.ArrayList<>();
            private final java.util.List<String> warnings = new java.util.ArrayList<>();
            private final java.util.List<String> info = new java.util.ArrayList<>();

            public Builder challengeId(Long challengeId) {
                this.challengeId = challengeId;
                return this;
            }

            public Builder challengeTitle(String challengeTitle) {
                this.challengeTitle = challengeTitle;
                return this;
            }

            public Builder valid(boolean valid) {
                this.valid = valid;
                return this;
            }

            public Builder addIssue(String issue) {
                this.issues.add(issue);
                return this;
            }

            public Builder addWarning(String warning) {
                this.warnings.add(warning);
                return this;
            }

            public Builder addInfo(String info) {
                this.info.add(info);
                return this;
            }

            public ChallengeValidationResult build() {
                return new ChallengeValidationResult(this);
            }
        }
    }
}