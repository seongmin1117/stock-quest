package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.ChallengeSessionJpaEntity;
import com.stockquest.adapter.out.persistence.repository.ChallengeSessionJpaRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 챌린지 세션 저장소 어댑터
 * 도메인 포트를 JPA Repository로 구현
 */
@Component
public class ChallengeSessionRepositoryAdapter implements ChallengeSessionRepository {

    private final ChallengeSessionJpaRepository jpaRepository;

    public ChallengeSessionRepositoryAdapter(ChallengeSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ChallengeSession save(ChallengeSession session) {
        ChallengeSessionJpaEntity entity = toEntity(session);
        ChallengeSessionJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ChallengeSession> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<ChallengeSession> findByChallengeIdAndUserId(Long challengeId, Long userId) {
        return jpaRepository.findByChallengeIdAndUserId(challengeId, userId)
                .map(this::toDomain);
    }

    @Override
    public List<ChallengeSession> findByChallengeId(Long challengeId) {
        return jpaRepository.findByChallengeId(challengeId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeSession> findByStatus(SessionStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeSession> findByUserIdOrderByStartedAtDesc(Long userId) {
        return jpaRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChallengeSession> findByUserIdAndChallengeIdAndStatus(Long userId, Long challengeId, SessionStatus status) {
        return jpaRepository.findByUserIdAndChallengeIdAndStatus(userId, challengeId, status)
                .map(this::toDomain);
    }

    @Override
    public List<ChallengeSession> findByChallengeIdAndStatus(Long challengeId, SessionStatus status) {
        return jpaRepository.findByChallengeIdAndStatus(challengeId, status)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeSession> findActiveSessionsOrderByStartedAtDesc() {
        return jpaRepository.findActiveSessionsOrderByStartedAtDesc()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeSession> findByUserIdAndStatus(Long userId, SessionStatus status) {
        return jpaRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeSession> findTopPerformersByChallengeId(Long challengeId, int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaRepository.findTopPerformersByChallengeId(challengeId, pageable)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeSession> findCompletedSessionsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findCompletedSessionsBetween(startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeSession> findRecentCompletedSessionsByUserId(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return jpaRepository.findRecentCompletedSessionsByUserId(userId, pageable)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByChallengeId(Long challengeId) {
        return jpaRepository.countByChallengeId(challengeId);
    }

    @Override
    public long countByChallengeIdAndStatus(Long challengeId, SessionStatus status) {
        return jpaRepository.countByChallengeIdAndStatus(challengeId, status);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteCompletedSessionsBeforeDate(LocalDateTime cutoffDate) {
        jpaRepository.deleteCompletedSessionsBeforeDate(cutoffDate);
    }

    @Override
    public List<ChallengeSession> findByChallengeIdOrderByReturnRateDesc(Long challengeId) {
        return jpaRepository.findByChallengeIdOrderByReturnRateDesc(challengeId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeSession> findByChallengeIdAndCreatedAtAfter(Long challengeId, LocalDateTime since) {
        return jpaRepository.findByChallengeIdAndCreatedAtAfter(challengeId, since)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * JPA Entity를 Domain Object로 변환
     */
    private ChallengeSession toDomain(ChallengeSessionJpaEntity entity) {
        return ChallengeSession.builder()
                .id(entity.getId())
                .challengeId(entity.getChallengeId())
                .userId(entity.getUserId())
                .status(entity.getStatus())
                .initialBalance(entity.getInitialBalance())
                .currentBalance(entity.getCurrentBalance())
                .returnRate(entity.getReturnRate())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Domain Object를 JPA Entity로 변환
     */
    private ChallengeSessionJpaEntity toEntity(ChallengeSession domain) {
        return ChallengeSessionJpaEntity.builder()
                .id(domain.getId())
                .challengeId(domain.getChallengeId())
                .userId(domain.getUserId())
                .status(domain.getStatus())
                .initialBalance(domain.getInitialBalance())
                .currentBalance(domain.getCurrentBalance())
                .returnRate(domain.getReturnRate())
                .startedAt(domain.getStartedAt())
                .completedAt(domain.getCompletedAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}