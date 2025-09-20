package com.stockquest.adapter.out.persistence.repository;

import com.stockquest.adapter.out.persistence.entity.CommunityCommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 커뮤니티 댓글 JPA Repository
 */
@Repository
public interface CommunityCommentJpaRepository extends JpaRepository<CommunityCommentJpaEntity, Long> {
    
    /**
     * 게시글별 댓글 목록 조회 (생성순)
     */
    List<CommunityCommentJpaEntity> findByPostIdOrderByCreatedAtAsc(Long postId);
    
    /**
     * 작성자별 댓글 목록 조회
     */
    List<CommunityCommentJpaEntity> findByAuthorId(Long authorId);
    
    /**
     * 게시글별 댓글 개수 조회
     */
    long countByPostId(Long postId);
}