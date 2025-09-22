package com.stockquest.application.community.port.in;

import com.stockquest.domain.community.CommunityComment;

/**
 * 댓글 수정 UseCase
 */
public interface UpdateCommentUseCase {

    CommunityComment updateComment(UpdateCommentCommand command);

    record UpdateCommentCommand(
            Long commentId,
            Long userId,  // 권한 검증용
            String content
    ) {}
}