package com.stockquest.domain.community.port;

import com.stockquest.domain.community.CommunityPost;

import java.util.List;
import java.util.Optional;

/**
 * 커뮤니티 게시글 저장소 포트
 */
public interface CommunityPostRepository {
    
    /**
     * 게시글 저장
     */
    CommunityPost save(CommunityPost post);
    
    /**
     * ID로 게시글 조회
     */
    Optional<CommunityPost> findById(Long id);
    
    /**
     * 챌린지별 게시글 목록 조회 (최신순)
     */
    List<CommunityPost> findByChallengeIdOrderByCreatedAtDesc(Long challengeId);
    
    /**
     * 작성자별 게시글 목록 조회
     */
    List<CommunityPost> findByAuthorId(Long authorId);
    
    /**
     * 게시글 삭제
     */
    void deleteById(Long id);
    
    /**
     * 챌린지별 게시글 개수 조회
     */
    long countByChallengeId(Long challengeId);
}