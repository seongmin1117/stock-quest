package com.stockquest.application.community;

import com.stockquest.application.community.port.in.GetCommentListUseCase;
import com.stockquest.domain.community.CommunityComment;
import com.stockquest.domain.community.port.CommunityCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글 목록 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCommentListService implements GetCommentListUseCase {
    
    private final CommunityCommentRepository communityCommentRepository;
    
    @Override
    public List<CommunityComment> getCommentList(GetCommentListQuery query) {
        return communityCommentRepository.findByPostIdOrderByCreatedAtAsc(query.postId());
    }
}