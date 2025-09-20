package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.LeaderboardJpaEntity;
import com.stockquest.adapter.out.persistence.repository.LeaderboardJpaRepository;
import com.stockquest.domain.leaderboard.LeaderboardEntry;
import com.stockquest.domain.leaderboard.port.LeaderboardRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 리더보드 저장소 어댑터
 * Domain LeaderboardRepository 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 */
@Component
public class LeaderboardRepositoryAdapter implements LeaderboardRepository {

    private final LeaderboardJpaRepository jpaRepository;

    public LeaderboardRepositoryAdapter(LeaderboardJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LeaderboardEntry save(LeaderboardEntry entry) {
        LeaderboardJpaEntity jpaEntity = LeaderboardJpaEntity.from(entry);
        LeaderboardJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<LeaderboardEntry> findById(Long id) {
        return jpaRepository.findById(id)
                .map(LeaderboardJpaEntity::toDomain);
    }

    @Override
    public List<LeaderboardEntry> findByChallengeIdOrderByReturnPercentageDesc(Long challengeId) {
        return jpaRepository.findByChallengeIdOrderByReturnPercentageDesc(challengeId)
                .stream()
                .map(LeaderboardJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LeaderboardEntry> findByChallengeIdAndUserId(Long challengeId, Long userId) {
        return jpaRepository.findByChallengeIdAndUserId(challengeId, userId)
                .map(LeaderboardJpaEntity::toDomain);
    }

    @Override
    public List<LeaderboardEntry> findTopNByChallengeId(Long challengeId, int limit) {
        return jpaRepository.findTopNByChallengeId(challengeId, limit)
                .stream()
                .map(LeaderboardJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByChallengeId(Long challengeId) {
        return jpaRepository.countByChallengeId(challengeId);
    }

    @Override
    public List<LeaderboardEntry> saveAll(List<LeaderboardEntry> entries) {
        List<LeaderboardJpaEntity> jpaEntities = entries.stream()
                .map(LeaderboardJpaEntity::from)
                .collect(Collectors.toList());

        List<LeaderboardJpaEntity> savedEntities = jpaRepository.saveAll(jpaEntities);

        return savedEntities.stream()
                .map(LeaderboardJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByChallengeId(Long challengeId) {
        jpaRepository.deleteByChallengeId(challengeId);
    }
}