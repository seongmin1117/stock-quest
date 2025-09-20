package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.community.CommunityComment;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 커뮤니티 댓글 JPA 엔티티
 */
@Entity
@Table(name = "community_comment")
@EntityListeners(AuditingEntityListener.class)
public class CommunityCommentJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "post_id", nullable = false)
    private Long postId;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    protected CommunityCommentJpaEntity() {}
    
    public CommunityCommentJpaEntity(Long postId, Long authorId, String content) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
    }
    
    public static CommunityCommentJpaEntity from(CommunityComment domainComment) {
        var entity = new CommunityCommentJpaEntity(
                domainComment.getPostId(),
                domainComment.getAuthorId(),
                domainComment.getContent()
        );
        entity.id = domainComment.getId();
        entity.createdAt = domainComment.getCreatedAt();
        entity.updatedAt = domainComment.getUpdatedAt();
        return entity;
    }
    
    public CommunityComment toDomain() {
        return CommunityComment.of(id, postId, authorId, content, createdAt, updatedAt);
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}