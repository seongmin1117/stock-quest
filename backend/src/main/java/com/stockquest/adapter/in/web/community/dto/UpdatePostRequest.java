package com.stockquest.adapter.in.web.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 수정 요청 DTO
 */
@Schema(description = "게시글 수정 요청")
public record UpdatePostRequest(
        @Schema(description = "게시글 내용", example = "수정된 게시글 내용입니다!")
        @NotBlank(message = "게시글 내용은 필수입니다")
        @Size(max = 2000, message = "게시글 내용은 2000자를 초과할 수 없습니다")
        String content
) {
}