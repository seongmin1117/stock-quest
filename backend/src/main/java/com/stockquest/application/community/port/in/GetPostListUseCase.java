package com.stockquest.application.community.port.in;

import com.stockquest.domain.community.CommunityPost;

import java.util.List;

/**
 * 게시글 목록 조회 유스케이스
 */
public interface GetPostListUseCase {
    
    List<CommunityPost> getPostList(GetPostListQuery query);
    
    record GetPostListQuery(
            Long challengeId
    ) {}
}