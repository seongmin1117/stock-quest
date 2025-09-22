package com.stockquest.application.community;

import com.stockquest.application.community.port.in.UpdatePostUseCase;
import com.stockquest.domain.community.CommunityPost;
import com.stockquest.domain.community.port.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 수정 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UpdatePostService implements UpdatePostUseCase {

    private final CommunityPostRepository communityPostRepository;

    @Override
    public CommunityPost updatePost(UpdatePostCommand command) {
        var post = communityPostRepository.findById(command.postId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        // 권한 확인: 작성자만 수정 가능
        if (!post.getAuthorId().equals(command.userId())) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다");
        }

        // 게시글 내용 업데이트
        post.updateContent(command.content());

        return communityPostRepository.save(post);
    }
}