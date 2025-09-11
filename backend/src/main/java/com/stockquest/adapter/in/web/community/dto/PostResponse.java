package com.stockquest.adapter.in.web.community.dto;

import com.stockquest.domain.community.CommunityPost;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 응답 DTO
 */
@Schema(description = "게시글 정보")
public record PostResponse(
        @Schema(description = "게시글 ID")
        Long id,
        
        @Schema(description = "챌린지 ID")
        Long challengeId,
        
        @Schema(description = "작성자 ID")
        Long authorId,
        
        @Schema(description = "게시글 내용")
        String content,
        
        @Schema(description = "작성일시")
        LocalDateTime createdAt,
        
        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
    public static PostResponse from(CommunityPost post) {
        return new PostResponse(
                post.getId(),
                post.getChallengeId(),
                post.getAuthorId(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
    
    public static List<PostResponse> from(List<CommunityPost> posts) {
        return posts.stream()
                .map(PostResponse::from)
                .toList();
    }
}