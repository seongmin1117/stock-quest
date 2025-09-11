package com.stockquest.adapter.in.web.community;

import com.stockquest.adapter.in.web.community.dto.*;
import com.stockquest.application.community.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 커뮤니티 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/challenges/{challengeId}")
@RequiredArgsConstructor
@Tag(name = "커뮤니티", description = "챌린지 커뮤니티 관련 API")
public class CommunityController {
    
    private final CreatePostUseCase createPostUseCase;
    private final GetPostListUseCase getPostListUseCase;
    private final CreateCommentUseCase createCommentUseCase;
    private final GetCommentListUseCase getCommentListUseCase;
    
    @PostMapping("/posts")
    @Operation(summary = "게시글 작성", description = "특정 챌린지에 게시글을 작성합니다")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable Long challengeId,
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        var command = new CreatePostUseCase.CreatePostCommand(
                challengeId,
                userId,
                request.content()
        );
        
        var post = createPostUseCase.createPost(command);
        return ResponseEntity.ok(PostResponse.from(post));
    }
    
    @GetMapping("/posts")
    @Operation(summary = "게시글 목록 조회", description = "특정 챌린지의 게시글 목록을 조회합니다")
    public ResponseEntity<List<PostResponse>> getPostList(
            @PathVariable Long challengeId) {
        
        var query = new GetPostListUseCase.GetPostListQuery(challengeId);
        var posts = getPostListUseCase.getPostList(query);
        
        return ResponseEntity.ok(PostResponse.from(posts));
    }
    
    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long challengeId,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        var command = new CreateCommentUseCase.CreateCommentCommand(
                postId,
                userId,
                request.content()
        );
        
        var comment = createCommentUseCase.createComment(command);
        return ResponseEntity.ok(CommentResponse.from(comment));
    }
    
    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 조회합니다")
    public ResponseEntity<List<CommentResponse>> getCommentList(
            @PathVariable Long challengeId,
            @PathVariable Long postId) {
        
        var query = new GetCommentListUseCase.GetCommentListQuery(postId);
        var comments = getCommentListUseCase.getCommentList(query);
        
        return ResponseEntity.ok(CommentResponse.from(comments));
    }
    
    private Long getUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}