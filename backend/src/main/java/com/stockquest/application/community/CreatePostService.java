package com.stockquest.application.community;

import com.stockquest.application.community.port.in.CreatePostUseCase;
import com.stockquest.domain.community.CommunityPost;
import com.stockquest.domain.community.port.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 작성 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreatePostService implements CreatePostUseCase {
    
    private final CommunityPostRepository communityPostRepository;
    
    @Override
    public CommunityPost createPost(CreatePostCommand command) {
        var post = new CommunityPost(
                command.challengeId(),
                command.authorId(),
                command.content()
        );
        
        return communityPostRepository.save(post);
    }
}