package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.ChallengeJpaEntity;
import com.stockquest.adapter.out.persistence.repository.ChallengeJpaRepository;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ChallengeRepository 구현체
 * 도메인 포트와 JPA 저장소를 연결하는 어댑터
 */
@Repository
@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepository {

    private final ChallengeJpaRepository jpaRepository;

    @Override
    public Challenge save(Challenge challenge) {
        ChallengeJpaEntity entity = ChallengeJpaEntity.fromDomain(challenge);
        ChallengeJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Challenge> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ChallengeJpaEntity::toDomain);
    }

    @Override
    public List<Challenge> findByStatus(ChallengeStatus status) {
        return jpaRepository.findByStatus(status)
            .stream()
            .map(ChallengeJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Challenge> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaRepository.findAll(pageable)
            .getContent()
            .stream()
            .map(ChallengeJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Challenge> findActiveChallenges() {
        return jpaRepository.findActiveChallenges()
            .stream()
            .map(ChallengeJpaEntity::toDomain)
            .collect(Collectors.toList());
    }
}