package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.challenge.ChallengeInstrument;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지 상품 JPA 엔티티
 */
@Entity
@Table(name = "challenge_instrument")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeInstrumentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "challenge_id")
    private Long challengeId;

    @Column(nullable = false, length = 1, name = "instrument_key")
    private String instrumentKey;

    @Column(nullable = false, length = 10, name = "actual_ticker")
    private String actualTicker;

    @Column(nullable = false, length = 100, name = "hidden_name")
    private String hiddenName;

    @Column(nullable = false, length = 100, name = "actual_name")
    private String actualName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeInstrument.InstrumentType type;

    /**
     * 도메인 객체로 변환
     */
    public ChallengeInstrument toDomain() {
        return ChallengeInstrument.builder()
                .id(this.id)
                .challengeId(this.challengeId)
                .instrumentKey(this.instrumentKey)
                .actualTicker(this.actualTicker)
                .hiddenName(this.hiddenName)
                .actualName(this.actualName)
                .type(this.type)
                .build();
    }

    /**
     * 도메인 객체로부터 생성
     */
    public static ChallengeInstrumentJpaEntity fromDomain(ChallengeInstrument domain) {
        return ChallengeInstrumentJpaEntity.builder()
                .id(domain.getId())
                .challengeId(domain.getChallengeId())
                .instrumentKey(domain.getInstrumentKey())
                .actualTicker(domain.getActualTicker())
                .hiddenName(domain.getHiddenName())
                .actualName(domain.getActualName())
                .type(domain.getType())
                .build();
    }
}