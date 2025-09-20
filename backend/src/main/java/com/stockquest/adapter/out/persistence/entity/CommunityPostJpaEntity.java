package com.stockquest.adapter.out.persistence.entity;

import com.stockquest.domain.community.CommunityPost;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 커뮤니티 게시글 JPA 엔티티
 */
@Entity
@Table(name = "community_post")
@EntityListeners(AuditingEntityListener.class)
public class CommunityPostJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;
    
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
    
    protected CommunityPostJpaEntity() {}
    
    public CommunityPostJpaEntity(Long challengeId, Long authorId, String content) {
        this.challengeId = challengeId;
        this.authorId = authorId;
        this.content = content;
    }
    
    public static CommunityPostJpaEntity from(CommunityPost domainPost) {
        var entity = new CommunityPostJpaEntity(
                domainPost.getChallengeId(),
                domainPost.getAuthorId(),
                domainPost.getContent()
        );
        entity.id = domainPost.getId();
        entity.createdAt = domainPost.getCreatedAt();
        entity.updatedAt = domainPost.getUpdatedAt();
        return entity;
    }
    
    public CommunityPost toDomain() {
        return CommunityPost.of(id, challengeId, authorId, content, createdAt, updatedAt);
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getChallengeId() { return challengeId; }
    public Long getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}