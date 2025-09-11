package com.stockquest.application.community;

import com.stockquest.application.community.port.in.CreateCommentUseCase;
import com.stockquest.domain.community.CommunityComment;
import com.stockquest.domain.community.port.CommunityCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글 작성 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreateCommentService implements CreateCommentUseCase {
    
    private final CommunityCommentRepository communityCommentRepository;
    
    @Override
    public CommunityComment createComment(CreateCommentCommand command) {
        var comment = new CommunityComment(
                command.postId(),
                command.authorId(),
                command.content()
        );
        
        return communityCommentRepository.save(comment);
    }
}