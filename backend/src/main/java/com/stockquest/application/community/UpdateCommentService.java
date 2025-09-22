package com.stockquest.application.community;

import com.stockquest.application.community.port.in.UpdateCommentUseCase;
import com.stockquest.domain.community.CommunityComment;
import com.stockquest.domain.community.port.CommunityCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글 수정 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateCommentService implements UpdateCommentUseCase {

    private final CommunityCommentRepository communityCommentRepository;

    @Override
    public CommunityComment updateComment(UpdateCommentCommand command) {
        var comment = communityCommentRepository.findById(command.commentId())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        // 권한 확인: 작성자만 수정 가능
        if (!comment.getAuthorId().equals(command.userId())) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다");
        }

        // 댓글 내용 업데이트
        comment.updateContent(command.content());

        return communityCommentRepository.save(comment);
    }
}