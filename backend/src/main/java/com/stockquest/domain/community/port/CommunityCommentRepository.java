package com.stockquest.domain.community.port;

import com.stockquest.domain.community.CommunityComment;

import java.util.List;
import java.util.Optional;

/**
 * 커뮤니티 댓글 저장소 포트
 */
public interface CommunityCommentRepository {
    
    /**
     * 댓글 저장
     */
    CommunityComment save(CommunityComment comment);
    
    /**
     * ID로 댓글 조회
     */
    Optional<CommunityComment> findById(Long id);
    
    /**
     * 게시글별 댓글 목록 조회 (생성순)
     */
    List<CommunityComment> findByPostIdOrderByCreatedAtAsc(Long postId);
    
    /**
     * 작성자별 댓글 목록 조회
     */
    List<CommunityComment> findByAuthorId(Long authorId);
    
    /**
     * 댓글 삭제
     */
    void deleteById(Long id);
    
    /**
     * 게시글별 댓글 개수 조회
     */
    long countByPostId(Long postId);
}