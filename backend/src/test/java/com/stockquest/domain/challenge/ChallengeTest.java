package com.stockquest.domain.challenge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Challenge 도메인 엔티티 핵심 비즈니스 로직 테스트
 */
@DisplayName("Challenge 도메인 테스트")
class ChallengeTest {

    @Nested
    @DisplayName("챌린지 상태 전환 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("DRAFT에서 유효한 상태로 전환 가능")
        void canTransitionFromDraft() {
            // Given
            Challenge challenge = createChallenge(ChallengeStatus.DRAFT);

            // When & Then
            assertThat(challenge.canTransitionTo(ChallengeStatus.SCHEDULED)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.ACTIVE)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.CANCELLED)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.COMPLETED)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.ARCHIVED)).isFalse();
        }

        @Test
        @DisplayName("SCHEDULED에서 유효한 상태로 전환 가능")
        void canTransitionFromScheduled() {
            // Given
            Challenge challenge = createChallenge(ChallengeStatus.SCHEDULED);

            // When & Then
            assertThat(challenge.canTransitionTo(ChallengeStatus.ACTIVE)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.CANCELLED)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.DRAFT)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.COMPLETED)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.ARCHIVED)).isFalse();
        }

        @Test
        @DisplayName("ACTIVE에서 유효한 상태로 전환 가능")
        void canTransitionFromActive() {
            // Given
            Challenge challenge = createChallenge(ChallengeStatus.ACTIVE);

            // When & Then
            assertThat(challenge.canTransitionTo(ChallengeStatus.COMPLETED)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.CANCELLED)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.DRAFT)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.SCHEDULED)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.ARCHIVED)).isFalse();
        }

        @Test
        @DisplayName("COMPLETED에서 ARCHIVED로만 전환 가능")
        void canTransitionFromCompleted() {
            // Given
            Challenge challenge = createChallenge(ChallengeStatus.COMPLETED);

            // When & Then
            assertThat(challenge.canTransitionTo(ChallengeStatus.ARCHIVED)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.DRAFT)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.SCHEDULED)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.ACTIVE)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("ARCHIVED에서 어떤 상태로도 전환 불가")
        void cannotTransitionFromArchived() {
            // Given
            Challenge challenge = createChallenge(ChallengeStatus.ARCHIVED);

            // When & Then
            for (ChallengeStatus status : ChallengeStatus.values()) {
                assertThat(challenge.canTransitionTo(status)).isFalse();
            }
        }

        @Test
        @DisplayName("CANCELLED에서 DRAFT로만 전환 가능")
        void canTransitionFromCancelled() {
            // Given
            Challenge challenge = createChallenge(ChallengeStatus.CANCELLED);

            // When & Then
            assertThat(challenge.canTransitionTo(ChallengeStatus.DRAFT)).isTrue();
            assertThat(challenge.canTransitionTo(ChallengeStatus.SCHEDULED)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.ACTIVE)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.COMPLETED)).isFalse();
            assertThat(challenge.canTransitionTo(ChallengeStatus.ARCHIVED)).isFalse();
        }
    }

    @Nested
    @DisplayName("챌린지 참여 가능 여부 테스트")
    class JoinableTest {

        @Test
        @DisplayName("ACTIVE 상태이고 참여자 수 제한 없으면 참여 가능")
        void isJoinableWhenActiveAndNoLimit() {
            // Given
            Challenge challenge = Challenge.builder()
                    .status(ChallengeStatus.ACTIVE)
                    .maxParticipants(null)
                    .currentParticipants(5)
                    .build();

            // When & Then
            assertThat(challenge.isJoinable()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE 상태이고 참여자 수가 최대치 미만이면 참여 가능")
        void isJoinableWhenActiveAndBelowLimit() {
            // Given
            Challenge challenge = Challenge.builder()
                    .status(ChallengeStatus.ACTIVE)
                    .maxParticipants(100)
                    .currentParticipants(50)
                    .build();

            // When & Then
            assertThat(challenge.isJoinable()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE 상태이지만 참여자 수가 최대치에 도달하면 참여 불가")
        void isNotJoinableWhenActiveButFull() {
            // Given
            Challenge challenge = Challenge.builder()
                    .status(ChallengeStatus.ACTIVE)
                    .maxParticipants(100)
                    .currentParticipants(100)
                    .build();

            // When & Then
            assertThat(challenge.isJoinable()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = ChallengeStatus.class, names = {"ACTIVE"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("ACTIVE 상태가 아니면 참여 불가")
        void isNotJoinableWhenNotActive(ChallengeStatus status) {
            // Given
            Challenge challenge = Challenge.builder()
                    .status(status)
                    .maxParticipants(100)
                    .currentParticipants(50)
                    .build();

            // When & Then
            assertThat(challenge.isJoinable()).isFalse();
        }
    }

    @Nested
    @DisplayName("참여자 수 관리 테스트")
    class ParticipantManagementTest {

        @Test
        @DisplayName("참여자 수 증가 - 초기값이 null인 경우")
        void incrementParticipantsFromNull() {
            // Given
            Challenge challenge = Challenge.builder()
                    .currentParticipants(null)
                    .build();

            // When
            challenge.incrementParticipants();

            // Then
            assertThat(challenge.getCurrentParticipants()).isEqualTo(1);
        }

        @Test
        @DisplayName("참여자 수 증가 - 기존값이 있는 경우")
        void incrementParticipantsFromExisting() {
            // Given
            Challenge challenge = Challenge.builder()
                    .currentParticipants(5)
                    .build();

            // When
            challenge.incrementParticipants();

            // Then
            assertThat(challenge.getCurrentParticipants()).isEqualTo(6);
        }

        @Test
        @DisplayName("참여자 수 감소 - 정상적인 경우")
        void decrementParticipantsNormal() {
            // Given
            Challenge challenge = Challenge.builder()
                    .currentParticipants(5)
                    .build();

            // When
            challenge.decrementParticipants();

            // Then
            assertThat(challenge.getCurrentParticipants()).isEqualTo(4);
        }

        @Test
        @DisplayName("참여자 수 감소 - 0인 경우 변화 없음")
        void decrementParticipantsFromZero() {
            // Given
            Challenge challenge = Challenge.builder()
                    .currentParticipants(0)
                    .build();

            // When
            challenge.decrementParticipants();

            // Then
            assertThat(challenge.getCurrentParticipants()).isEqualTo(0);
        }

        @Test
        @DisplayName("참여자 수 감소 - null인 경우 변화 없음")
        void decrementParticipantsFromNull() {
            // Given
            Challenge challenge = Challenge.builder()
                    .currentParticipants(null)
                    .build();

            // When
            challenge.decrementParticipants();

            // Then
            assertThat(challenge.getCurrentParticipants()).isNull();
        }
    }

    @Nested
    @DisplayName("난이도 및 리스크 확인 테스트")
    class DifficultyAndRiskTest {

        @ParameterizedTest
        @EnumSource(value = ChallengeDifficulty.class, names = {"ADVANCED", "EXPERT"})
        @DisplayName("ADVANCED, EXPERT는 고난이도")
        void isHighDifficultyForAdvancedAndExpert(ChallengeDifficulty difficulty) {
            // Given
            Challenge challenge = Challenge.builder()
                    .difficulty(difficulty)
                    .build();

            // When & Then
            assertThat(challenge.isHighDifficulty()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = ChallengeDifficulty.class, names = {"ADVANCED", "EXPERT"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("BEGINNER, INTERMEDIATE는 고난이도 아님")
        void isNotHighDifficultyForBeginnerAndIntermediate(ChallengeDifficulty difficulty) {
            // Given
            Challenge challenge = Challenge.builder()
                    .difficulty(difficulty)
                    .build();

            // When & Then
            assertThat(challenge.isHighDifficulty()).isFalse();
        }

        @ParameterizedTest
        @MethodSource("provideHighRiskLevels")
        @DisplayName("리스크 레벨 8 이상은 고위험")
        void isHighRiskForLevel8AndAbove(Integer riskLevel) {
            // Given
            Challenge challenge = Challenge.builder()
                    .riskLevel(riskLevel)
                    .build();

            // When & Then
            assertThat(challenge.isHighRisk()).isTrue();
        }

        @ParameterizedTest
        @MethodSource("provideLowRiskLevels")
        @DisplayName("리스크 레벨 7 이하는 고위험 아님")
        void isNotHighRiskForLevel7AndBelow(Integer riskLevel) {
            // Given
            Challenge challenge = Challenge.builder()
                    .riskLevel(riskLevel)
                    .build();

            // When & Then
            assertThat(challenge.isHighRisk()).isFalse();
        }

        @Test
        @DisplayName("리스크 레벨이 null이면 고위험 아님")
        void isNotHighRiskForNullLevel() {
            // Given
            Challenge challenge = Challenge.builder()
                    .riskLevel(null)
                    .build();

            // When & Then
            assertThat(challenge.isHighRisk()).isFalse();
        }

        static Stream<Arguments> provideHighRiskLevels() {
            return Stream.of(
                Arguments.of(8),
                Arguments.of(9),
                Arguments.of(10)
            );
        }

        static Stream<Arguments> provideLowRiskLevels() {
            return Stream.of(
                Arguments.of(1),
                Arguments.of(5),
                Arguments.of(7)
            );
        }
    }

    @Nested
    @DisplayName("챌린지 기간 확인 테스트")
    class DurationTest {

        @Test
        @DisplayName("60일 초과는 장기 챌린지")
        void isLongTermForMoreThan60Days() {
            // Given
            Challenge challenge = Challenge.builder()
                    .durationDays(90)
                    .build();

            // When & Then
            assertThat(challenge.isLongTerm()).isTrue();
        }

        @Test
        @DisplayName("60일 이하는 장기 챌린지 아님")
        void isNotLongTermFor60DaysOrLess() {
            // Given
            Challenge challenge = Challenge.builder()
                    .durationDays(30)
                    .build();

            // When & Then
            assertThat(challenge.isLongTerm()).isFalse();
        }

        @Test
        @DisplayName("기간이 null이면 장기 챌린지 아님")
        void isNotLongTermForNullDuration() {
            // Given
            Challenge challenge = Challenge.builder()
                    .durationDays(null)
                    .build();

            // When & Then
            assertThat(challenge.isLongTerm()).isFalse();
        }
    }

    @Nested
    @DisplayName("챌린지 타입별 특성 테스트")
    class ChallengeTypeTest {

        @Test
        @DisplayName("TOURNAMENT 타입 확인")
        void isTournamentType() {
            // Given
            Challenge challenge = Challenge.builder()
                    .challengeType(ChallengeType.TOURNAMENT)
                    .build();

            // When & Then
            assertThat(challenge.isTournament()).isTrue();
            assertThat(challenge.isEducational()).isFalse();
            assertThat(challenge.isCommunity()).isFalse();
        }

        @Test
        @DisplayName("EDUCATIONAL 타입 확인")
        void isEducationalType() {
            // Given
            Challenge challenge = Challenge.builder()
                    .challengeType(ChallengeType.EDUCATIONAL)
                    .build();

            // When & Then
            assertThat(challenge.isEducational()).isTrue();
            assertThat(challenge.isTournament()).isFalse();
            assertThat(challenge.isCommunity()).isFalse();
        }

        @Test
        @DisplayName("COMMUNITY 타입 확인")
        void isCommunityType() {
            // Given
            Challenge challenge = Challenge.builder()
                    .challengeType(ChallengeType.COMMUNITY)
                    .build();

            // When & Then
            assertThat(challenge.isCommunity()).isTrue();
            assertThat(challenge.isTournament()).isFalse();
            assertThat(challenge.isEducational()).isFalse();
        }
    }

    @Nested
    @DisplayName("평점 및 인기도 테스트")
    class RatingAndPopularityTest {

        @Test
        @DisplayName("평점 업데이트 시 updatedAt 갱신")
        void updateRatingUpdatesTimestamp() {
            // Given
            Challenge challenge = Challenge.builder()
                    .averageRating(BigDecimal.valueOf(3.5))
                    .totalReviews(10)
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build();

            LocalDateTime beforeUpdate = challenge.getUpdatedAt();

            // When
            challenge.updateRating(BigDecimal.valueOf(4.2), 15);

            // Then
            assertThat(challenge.getAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(4.2));
            assertThat(challenge.getTotalReviews()).isEqualTo(15);
            assertThat(challenge.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("참여자 20명 초과, 평점 4.0 초과면 인기 챌린지")
        void isPopularWhenHighParticipantsAndRating() {
            // Given
            Challenge challenge = Challenge.builder()
                    .currentParticipants(25)
                    .averageRating(BigDecimal.valueOf(4.5))
                    .build();

            // When & Then
            assertThat(challenge.isPopular()).isTrue();
        }

        @Test
        @DisplayName("참여자 20명 이하면 인기 챌린지 아님")
        void isNotPopularWhenLowParticipants() {
            // Given
            Challenge challenge = Challenge.builder()
                    .currentParticipants(15)
                    .averageRating(BigDecimal.valueOf(4.5))
                    .build();

            // When & Then
            assertThat(challenge.isPopular()).isFalse();
        }

        @Test
        @DisplayName("평점 4.0 이하면 인기 챌린지 아님")
        void isNotPopularWhenLowRating() {
            // Given
            Challenge challenge = Challenge.builder()
                    .currentParticipants(25)
                    .averageRating(BigDecimal.valueOf(3.5))
                    .build();

            // When & Then
            assertThat(challenge.isPopular()).isFalse();
        }
    }

    @Nested
    @DisplayName("버전 관리 테스트")
    class VersionManagementTest {

        @Test
        @DisplayName("버전 증가 시 updatedAt 갱신")
        void incrementVersionUpdatesTimestamp() {
            // Given
            Challenge challenge = Challenge.builder()
                    .version(1)
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build();

            LocalDateTime beforeUpdate = challenge.getUpdatedAt();

            // When
            challenge.incrementVersion();

            // Then
            assertThat(challenge.getVersion()).isEqualTo(2);
            assertThat(challenge.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("버전이 null이면 1로 설정")
        void incrementVersionFromNull() {
            // Given
            Challenge challenge = Challenge.builder()
                    .version(null)
                    .build();

            // When
            challenge.incrementVersion();

            // Then
            assertThat(challenge.getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("수정자 업데이트 시 버전도 증가")
        void updateModifiedByIncrementsVersion() {
            // Given
            Challenge challenge = Challenge.builder()
                    .version(5)
                    .lastModifiedBy(100L)
                    .build();

            // When
            challenge.updateModifiedBy(200L);

            // Then
            assertThat(challenge.getLastModifiedBy()).isEqualTo(200L);
            assertThat(challenge.getVersion()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("기타 비즈니스 로직 테스트")
    class OtherBusinessLogicTest {

        @Test
        @DisplayName("진입 요구사항 존재 여부 확인")
        void hasEntryRequirementsWhenNotEmpty() {
            // Given
            Challenge challenge = Challenge.builder()
                    .entryRequirements(java.util.Map.of("minLevel", 5, "experience", "INTERMEDIATE"))
                    .build();

            // When & Then
            assertThat(challenge.hasEntryRequirements()).isTrue();
        }

        @Test
        @DisplayName("진입 요구사항이 null이면 false")
        void hasNoEntryRequirementsWhenNull() {
            // Given
            Challenge challenge = Challenge.builder()
                    .entryRequirements(null)
                    .build();

            // When & Then
            assertThat(challenge.hasEntryRequirements()).isFalse();
        }

        @Test
        @DisplayName("특정 상품 사용 가능 여부 확인")
        void isInstrumentAvailableWhenInList() {
            // Given
            Challenge challenge = Challenge.builder()
                    .availableInstruments(List.of("AAPL", "GOOGL", "MSFT"))
                    .build();

            // When & Then
            assertThat(challenge.isInstrumentAvailable("AAPL")).isTrue();
            assertThat(challenge.isInstrumentAvailable("TSLA")).isFalse();
        }

        @Test
        @DisplayName("피처드 챌린지 설정")
        void setFeaturedUpdatesTimestamp() {
            // Given
            Challenge challenge = Challenge.builder()
                    .featured(false)
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build();

            LocalDateTime beforeUpdate = challenge.getUpdatedAt();

            // When
            challenge.setFeatured(true);

            // Then
            assertThat(challenge.getFeatured()).isTrue();
            assertThat(challenge.getUpdatedAt()).isAfter(beforeUpdate);
        }
    }

    // 테스트 헬퍼 메서드
    private Challenge createChallenge(ChallengeStatus status) {
        return Challenge.builder()
                .id(1L)
                .title("Test Challenge")
                .status(status)
                .difficulty(ChallengeDifficulty.BEGINNER)
                .challengeType(ChallengeType.EDUCATIONAL)
                .initialBalance(BigDecimal.valueOf(1000000))
                .durationDays(30)
                .maxParticipants(100)
                .currentParticipants(0)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}