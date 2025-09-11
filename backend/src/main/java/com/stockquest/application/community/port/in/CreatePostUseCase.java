package com.stockquest.application.community.port.in;

import com.stockquest.domain.community.CommunityPost;

/**
 * 게시글 작성 유스케이스
 */
public interface CreatePostUseCase {
    
    CommunityPost createPost(CreatePostCommand command);
    
    record CreatePostCommand(
            Long challengeId,
            Long authorId,
            String content
    ) {}
}