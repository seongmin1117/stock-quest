package com.stockquest.application.community.port.in;

import com.stockquest.domain.community.CommunityComment;

import java.util.List;

/**
 * 댓글 목록 조회 유스케이스
 */
public interface GetCommentListUseCase {
    
    List<CommunityComment> getCommentList(GetCommentListQuery query);
    
    record GetCommentListQuery(
            Long postId
    ) {}
}