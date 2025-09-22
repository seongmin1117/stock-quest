package com.stockquest.adapter.in.web.community;

import com.stockquest.adapter.in.web.community.dto.*;
import com.stockquest.application.community.port.in.*;
import com.stockquest.application.security.SecureUserContextService;
import com.stockquest.domain.user.port.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final CreateCommentUseCase createCommentUseCase;
    private final GetCommentListUseCase getCommentListUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final SecureUserContextService secureUserContextService;
    private final UserRepository userRepository;
    
    @PostMapping("/posts")
    @Operation(summary = "게시글 작성", description = "특정 챌린지에 게시글을 작성합니다")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable Long challengeId,
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = secureUserContextService.getCurrentUserId(userDetails);
        var command = new CreatePostUseCase.CreatePostCommand(
                challengeId,
                userId,
                request.content()
        );
        
        var post = createPostUseCase.createPost(command);
        var authorNickname = userRepository.findById(post.getAuthorId())
                .map(user -> user.getNickname())
                .orElse("Unknown");
        return ResponseEntity.ok(PostResponse.from(post, authorNickname));
    }
    
    @GetMapping("/posts")
    @Operation(summary = "게시글 목록 조회", description = "특정 챌린지의 게시글 목록을 조회합니다")
    public ResponseEntity<List<PostResponse>> getPostList(
            @PathVariable Long challengeId) {

        var query = new GetPostListUseCase.GetPostListQuery(challengeId);
        var posts = getPostListUseCase.getPostList(query);

        // 작성자 정보를 조회하여 PostResponse에 포함
        var authorIds = posts.stream()
                .map(post -> post.getAuthorId())
                .distinct()
                .toList();

        var authorMap = userRepository.findByIdIn(authorIds)
                .stream()
                .collect(Collectors.toMap(
                        user -> user.getId(),
                        user -> user.getNickname()
                ));

        var postResponses = posts.stream()
                .map(post -> PostResponse.from(
                        post,
                        authorMap.getOrDefault(post.getAuthorId(), "Unknown")
                ))
                .toList();

        return ResponseEntity.ok(postResponses);
    }

    @PutMapping("/posts/{postId}")
    @Operation(summary = "게시글 수정", description = "특정 게시글의 내용을 수정합니다")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long challengeId,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = secureUserContextService.getCurrentUserId(userDetails);
        var command = new UpdatePostUseCase.UpdatePostCommand(
                postId,
                userId,
                request.content()
        );

        var post = updatePostUseCase.updatePost(command);
        var authorNickname = userRepository.findById(post.getAuthorId())
                .map(user -> user.getNickname())
                .orElse("Unknown");
        return ResponseEntity.ok(PostResponse.from(post, authorNickname));
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long challengeId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = secureUserContextService.getCurrentUserId(userDetails);
        var command = new DeletePostUseCase.DeletePostCommand(postId, userId);

        deletePostUseCase.deletePost(command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long challengeId,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = secureUserContextService.getCurrentUserId(userDetails);
        var command = new CreateCommentUseCase.CreateCommentCommand(
                postId,
                userId,
                request.content()
        );
        
        var comment = createCommentUseCase.createComment(command);
        var authorNickname = userRepository.findById(comment.getAuthorId())
                .map(user -> user.getNickname())
                .orElse("Unknown");
        return ResponseEntity.ok(CommentResponse.from(comment, authorNickname));
    }
    
    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 조회합니다")
    public ResponseEntity<List<CommentResponse>> getCommentList(
            @PathVariable Long challengeId,
            @PathVariable Long postId) {
        
        var query = new GetCommentListUseCase.GetCommentListQuery(postId);
        var comments = getCommentListUseCase.getCommentList(query);

        // 작성자 정보를 조회하여 CommentResponse에 포함
        var authorIds = comments.stream()
                .map(comment -> comment.getAuthorId())
                .distinct()
                .toList();

        var authorMap = userRepository.findByIdIn(authorIds)
                .stream()
                .collect(Collectors.toMap(
                        user -> user.getId(),
                        user -> user.getNickname()
                ));

        var commentResponses = comments.stream()
                .map(comment -> CommentResponse.from(
                        comment,
                        authorMap.getOrDefault(comment.getAuthorId(), "Unknown")
                ))
                .toList();

        return ResponseEntity.ok(commentResponses);
    }

    @PutMapping("/posts/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 수정", description = "특정 댓글의 내용을 수정합니다")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long challengeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = secureUserContextService.getCurrentUserId(userDetails);
        var command = new UpdateCommentUseCase.UpdateCommentCommand(
                commentId,
                userId,
                request.content()
        );

        var comment = updateCommentUseCase.updateComment(command);
        var authorNickname = userRepository.findById(comment.getAuthorId())
                .map(user -> user.getNickname())
                .orElse("Unknown");
        return ResponseEntity.ok(CommentResponse.from(comment, authorNickname));
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long challengeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = secureUserContextService.getCurrentUserId(userDetails);
        var command = new DeleteCommentUseCase.DeleteCommentCommand(commentId, userId);

        deleteCommentUseCase.deleteComment(command);
        return ResponseEntity.noContent().build();
    }

}