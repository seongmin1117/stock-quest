package com.stockquest.application.community;

import com.stockquest.application.community.port.in.GetPostListUseCase;
import com.stockquest.domain.community.CommunityPost;
import com.stockquest.domain.community.port.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시글 목록 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPostListService implements GetPostListUseCase {
    
    private final CommunityPostRepository communityPostRepository;
    
    @Override
    public List<CommunityPost> getPostList(GetPostListQuery query) {
        return communityPostRepository.findByChallengeIdOrderByCreatedAtDesc(query.challengeId());
    }
}