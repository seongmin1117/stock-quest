package com.stockquest.application.community.port.in;

import com.stockquest.domain.community.CommunityComment;

/**
 * 댓글 작성 유스케이스
 */
public interface CreateCommentUseCase {
    
    CommunityComment createComment(CreateCommentCommand command);
    
    record CreateCommentCommand(
            Long postId,
            Long authorId,
            String content
    ) {}
}