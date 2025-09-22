package com.stockquest.application.community;

import com.stockquest.application.community.port.in.DeletePostUseCase;
import com.stockquest.domain.community.port.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 삭제 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DeletePostService implements DeletePostUseCase {

    private final CommunityPostRepository communityPostRepository;

    @Override
    public void deletePost(DeletePostCommand command) {
        var post = communityPostRepository.findById(command.postId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        // 권한 확인: 작성자만 삭제 가능
        if (!post.getAuthorId().equals(command.userId())) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다");
        }

        communityPostRepository.deleteById(command.postId());
    }
}