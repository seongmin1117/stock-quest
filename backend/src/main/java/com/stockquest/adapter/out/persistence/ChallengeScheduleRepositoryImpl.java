package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.ChallengeScheduleJpaEntity;
import com.stockquest.adapter.out.persistence.repository.ChallengeScheduleJpaRepository;
import com.stockquest.domain.challenge.ChallengeSchedule;
import com.stockquest.domain.challenge.port.ChallengeScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ChallengeScheduleRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ChallengeScheduleRepositoryImpl implements ChallengeScheduleRepository {
    
    private final ChallengeScheduleJpaRepository jpaRepository;
    
    @Override
    public ChallengeSchedule save(ChallengeSchedule schedule) {
        ChallengeScheduleJpaEntity entity = toEntity(schedule);
        ChallengeScheduleJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Optional<ChallengeSchedule> findById(Long id) {
        return jpaRepository.findById(id)
            .map(this::toDomain);
    }
    
    @Override
    public List<ChallengeSchedule> findByChallengeId(Long challengeId) {
        return jpaRepository.findByChallengeId(challengeId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeSchedule> findSchedulesToActivate(LocalDateTime currentTime) {
        return jpaRepository.findSchedulesToActivate(currentTime)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeSchedule> findSchedulesToDeactivate(LocalDateTime currentTime) {
        return jpaRepository.findSchedulesToDeactivate(currentTime)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeSchedule> findRecurringSchedules() {
        return jpaRepository.findByScheduleType(ChallengeSchedule.ScheduleType.RECURRING)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChallengeSchedule> findActiveSchedules() {
        return jpaRepository.findActiveSchedules()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ChallengeSchedule> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
            .map(this::toDomain);
    }
    
    @Override
    public List<ChallengeSchedule> findByActivationDateBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByActivationDateBetween(start, end)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(ChallengeSchedule schedule) {
        ChallengeScheduleJpaEntity entity = toEntity(schedule);
        jpaRepository.delete(entity);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public boolean existsActiveByChallengeId(Long challengeId) {
        return jpaRepository.existsActiveByChallengeId(challengeId);
    }
    
    // 엔티티 변환 메서드들
    private ChallengeSchedule toDomain(ChallengeScheduleJpaEntity entity) {
        return ChallengeSchedule.builder()
            .id(entity.getId())
            .challengeId(entity.getChallengeId())
            .scheduleType(entity.getScheduleType())
            .recurrencePattern(entity.getRecurrencePattern())
            .activationDate(entity.getActivationDate())
            .deactivationDate(entity.getDeactivationDate())
            .timezone(entity.getTimezone())
            .isActive(entity.getIsActive())
            .metadata(entity.getMetadata())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    private ChallengeScheduleJpaEntity toEntity(ChallengeSchedule domain) {
        ChallengeScheduleJpaEntity entity = new ChallengeScheduleJpaEntity();
        entity.setId(domain.getId());
        entity.setChallengeId(domain.getChallengeId());
        entity.setScheduleType(domain.getScheduleType());
        entity.setRecurrencePattern(domain.getRecurrencePattern());
        entity.setActivationDate(domain.getActivationDate());
        entity.setDeactivationDate(domain.getDeactivationDate());
        entity.setTimezone(domain.getTimezone());
        entity.setIsActive(domain.getIsActive());
        entity.setMetadata(domain.getMetadata());
        return entity;
    }
}