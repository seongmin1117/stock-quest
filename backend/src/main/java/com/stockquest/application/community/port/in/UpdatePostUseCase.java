package com.stockquest.application.community.port.in;

import com.stockquest.domain.community.CommunityPost;

/**
 * 게시글 수정 UseCase
 */
public interface UpdatePostUseCase {

    CommunityPost updatePost(UpdatePostCommand command);

    record UpdatePostCommand(
            Long postId,
            Long userId,  // 권한 검증용
            String content
    ) {}
}