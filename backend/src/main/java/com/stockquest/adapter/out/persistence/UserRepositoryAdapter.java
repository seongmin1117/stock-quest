package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.UserJpaEntity;
import com.stockquest.adapter.out.persistence.repository.UserJpaRepository;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 저장소 어댑터
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaRepository.existsByNickname(nickname);
    }

    /**
     * JPA Entity를 Domain Object로 변환
     */
    private User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .passwordHash(entity.getPasswordHash())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Domain Object를 JPA Entity로 변환
     */
    private UserJpaEntity toEntity(User domain) {
        return UserJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .nickname(domain.getNickname())
                .passwordHash(domain.getPasswordHash())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}