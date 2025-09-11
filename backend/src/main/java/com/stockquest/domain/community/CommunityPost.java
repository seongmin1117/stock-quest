package com.stockquest.domain.community;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 커뮤니티 게시글 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CommunityPost {
    private Long id;
    private Long challengeId;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
    public CommunityPost(Long challengeId, Long authorId, String content) {
        validateChallengeId(challengeId);
        validateAuthorId(authorId);
        validateContent(content);
        
        this.challengeId = challengeId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    private void validateChallengeId(Long challengeId) {
        if (challengeId == null || challengeId <= 0) {
            throw new IllegalArgumentException("유효한 챌린지 ID가 필요합니다");
        }
    }
    
    private void validateAuthorId(Long authorId) {
        if (authorId == null || authorId <= 0) {
            throw new IllegalArgumentException("유효한 작성자 ID가 필요합니다");
        }
    }
    
    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("게시글 내용은 필수입니다");
        }
        if (content.length() > 2000) {
            throw new IllegalArgumentException("게시글 내용은 2000자를 초과할 수 없습니다");
        }
    }
    
    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 정적 팩토리 메소드 (JPA Entity에서 도메인 객체 생성용)
    public static CommunityPost of(Long id, Long challengeId, Long authorId, String content, 
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        var post = new CommunityPost(challengeId, authorId, content);
        post.id = id;
        post.createdAt = createdAt;
        post.updatedAt = updatedAt;
        return post;
    }
    
}