package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.CommunityPostJpaEntity;
import com.stockquest.adapter.out.persistence.repository.CommunityPostJpaRepository;
import com.stockquest.domain.community.CommunityPost;
import com.stockquest.domain.community.port.CommunityPostRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 커뮤니티 게시글 저장소 어댑터
 * Domain CommunityPostRepository 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 */
@Component
public class CommunityPostRepositoryAdapter implements CommunityPostRepository {

    private final CommunityPostJpaRepository jpaRepository;

    public CommunityPostRepositoryAdapter(CommunityPostJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CommunityPost save(CommunityPost post) {
        CommunityPostJpaEntity jpaEntity = CommunityPostJpaEntity.from(post);
        CommunityPostJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<CommunityPost> findById(Long id) {
        return jpaRepository.findById(id)
                .map(CommunityPostJpaEntity::toDomain);
    }

    @Override
    public List<CommunityPost> findByChallengeIdOrderByCreatedAtDesc(Long challengeId) {
        return jpaRepository.findByChallengeIdOrderByCreatedAtDesc(challengeId)
                .stream()
                .map(CommunityPostJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunityPost> findByAuthorId(Long authorId) {
        return jpaRepository.findByAuthorId(authorId)
                .stream()
                .map(CommunityPostJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByChallengeId(Long challengeId) {
        return jpaRepository.countByChallengeId(challengeId);
    }
}