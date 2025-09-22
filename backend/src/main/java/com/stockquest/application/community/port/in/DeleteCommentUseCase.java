package com.stockquest.application.community.port.in;

/**
 * 댓글 삭제 UseCase
 */
public interface DeleteCommentUseCase {

    void deleteComment(DeleteCommentCommand command);

    record DeleteCommentCommand(
            Long commentId,
            Long userId  // 권한 검증용
    ) {}
}