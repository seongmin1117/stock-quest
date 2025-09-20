package com.stockquest.adapter.out.persistence;

import com.stockquest.adapter.out.persistence.entity.CommunityCommentJpaEntity;
import com.stockquest.adapter.out.persistence.repository.CommunityCommentJpaRepository;
import com.stockquest.domain.community.CommunityComment;
import com.stockquest.domain.community.port.CommunityCommentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 커뮤니티 댓글 저장소 어댑터
 * Domain CommunityCommentRepository 인터페이스를 구현하여 JPA를 통한 데이터 영속성 제공
 */
@Component
public class CommunityCommentRepositoryAdapter implements CommunityCommentRepository {

    private final CommunityCommentJpaRepository jpaRepository;

    public CommunityCommentRepositoryAdapter(CommunityCommentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CommunityComment save(CommunityComment comment) {
        CommunityCommentJpaEntity jpaEntity = CommunityCommentJpaEntity.from(comment);
        CommunityCommentJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<CommunityComment> findById(Long id) {
        return jpaRepository.findById(id)
                .map(CommunityCommentJpaEntity::toDomain);
    }

    @Override
    public List<CommunityComment> findByPostIdOrderByCreatedAtAsc(Long postId) {
        return jpaRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(CommunityCommentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunityComment> findByAuthorId(Long authorId) {
        return jpaRepository.findByAuthorId(authorId)
                .stream()
                .map(CommunityCommentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByPostId(Long postId) {
        return jpaRepository.countByPostId(postId);
    }
}