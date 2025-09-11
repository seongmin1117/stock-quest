package com.stockquest.domain.community;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 커뮤니티 댓글 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CommunityComment {
    private Long id;
    private Long postId;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
    public CommunityComment(Long postId, Long authorId, String content) {
        validatePostId(postId);
        validateAuthorId(authorId);
        validateContent(content);
        
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("유효한 게시글 ID가 필요합니다");
        }
    }
    
    private void validateAuthorId(Long authorId) {
        if (authorId == null || authorId <= 0) {
            throw new IllegalArgumentException("유효한 작성자 ID가 필요합니다");
        }
    }
    
    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("댓글 내용은 1000자를 초과할 수 없습니다");
        }
    }
    
    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 정적 팩토리 메소드 (JPA Entity에서 도메인 객체 생성용)
    public static CommunityComment of(Long id, Long postId, Long authorId, String content,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        var comment = new CommunityComment(postId, authorId, content);
        comment.id = id;
        comment.createdAt = createdAt;
        comment.updatedAt = updatedAt;
        return comment;
    }
    
}