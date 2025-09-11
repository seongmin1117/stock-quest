package com.stockquest.adapter.in.web.community.dto;

import com.stockquest.domain.community.CommunityComment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 응답 DTO
 */
@Schema(description = "댓글 정보")
public record CommentResponse(
        @Schema(description = "댓글 ID")
        Long id,
        
        @Schema(description = "게시글 ID")
        Long postId,
        
        @Schema(description = "작성자 ID")
        Long authorId,
        
        @Schema(description = "댓글 내용")
        String content,
        
        @Schema(description = "작성일시")
        LocalDateTime createdAt,
        
        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
    public static CommentResponse from(CommunityComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
    
    public static List<CommentResponse> from(List<CommunityComment> comments) {
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }
}