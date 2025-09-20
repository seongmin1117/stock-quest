package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.ChallengeInstrumentJpaEntity;
import com.stockquest.adapter.out.persistence.repository.ChallengeInstrumentJpaRepository;
import com.stockquest.domain.challenge.ChallengeInstrument;
import com.stockquest.domain.challenge.port.ChallengeInstrumentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 챌린지 상품 저장소 어댑터
 * Domain ChallengeInstrumentRepository 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 */
@Component
public class ChallengeInstrumentRepositoryAdapter implements ChallengeInstrumentRepository {

    private final ChallengeInstrumentJpaRepository jpaRepository;

    public ChallengeInstrumentRepositoryAdapter(ChallengeInstrumentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ChallengeInstrument> findByChallengeId(Long challengeId) {
        return jpaRepository.findByChallengeId(challengeId)
                .stream()
                .map(ChallengeInstrumentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChallengeInstrument> findByChallengeIdAndInstrumentKey(Long challengeId, String instrumentKey) {
        return jpaRepository.findByChallengeIdAndInstrumentKey(challengeId, instrumentKey)
                .map(ChallengeInstrumentJpaEntity::toDomain);
    }

    @Override
    public ChallengeInstrument save(ChallengeInstrument instrument) {
        ChallengeInstrumentJpaEntity jpaEntity = ChallengeInstrumentJpaEntity.fromDomain(instrument);
        ChallengeInstrumentJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }

    @Override
    public List<ChallengeInstrument> saveAll(List<ChallengeInstrument> instruments) {
        List<ChallengeInstrumentJpaEntity> jpaEntities = instruments.stream()
                .map(ChallengeInstrumentJpaEntity::fromDomain)
                .collect(Collectors.toList());

        List<ChallengeInstrumentJpaEntity> savedEntities = jpaRepository.saveAll(jpaEntities);

        return savedEntities.stream()
                .map(ChallengeInstrumentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChallengeInstrument> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ChallengeInstrumentJpaEntity::toDomain);
    }

    @Override
    public void delete(ChallengeInstrument instrument) {
        if (instrument.getId() != null) {
            jpaRepository.deleteById(instrument.getId());
        }
    }
}