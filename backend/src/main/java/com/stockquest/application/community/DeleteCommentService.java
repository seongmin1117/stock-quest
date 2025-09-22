package com.stockquest.application.community;

import com.stockquest.application.community.port.in.DeleteCommentUseCase;
import com.stockquest.domain.community.port.CommunityCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글 삭제 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteCommentService implements DeleteCommentUseCase {

    private final CommunityCommentRepository communityCommentRepository;

    @Override
    public void deleteComment(DeleteCommentCommand command) {
        var comment = communityCommentRepository.findById(command.commentId())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        // 권한 확인: 작성자만 삭제 가능
        if (!comment.getAuthorId().equals(command.userId())) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다");
        }

        communityCommentRepository.deleteById(command.commentId());
    }
}