package com.stockquest.application.community.port.in;

/**
 * 게시글 삭제 UseCase
 */
public interface DeletePostUseCase {

    void deletePost(DeletePostCommand command);

    record DeletePostCommand(
            Long postId,
            Long userId  // 권한 검증용
    ) {}
}