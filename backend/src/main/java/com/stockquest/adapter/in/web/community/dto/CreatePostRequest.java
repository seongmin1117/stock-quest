package com.stockquest.adapter.in.web.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 작성 요청 DTO
 */
@Schema(description = "게시글 작성 요청")
public record CreatePostRequest(
        @Schema(description = "게시글 내용", example = "이번 챌린지에서 테슬라 주식에 투자해봤습니다!")
        @NotBlank(message = "게시글 내용은 필수입니다")
        @Size(max = 2000, message = "게시글 내용은 2000자를 초과할 수 없습니다")
        String content
) {
}