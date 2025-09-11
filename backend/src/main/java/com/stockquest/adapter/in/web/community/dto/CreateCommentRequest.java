package com.stockquest.adapter.in.web.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 댓글 작성 요청 DTO
 */
@Schema(description = "댓글 작성 요청")
public record CreateCommentRequest(
        @Schema(description = "댓글 내용", example = "좋은 전략이네요!")
        @NotBlank(message = "댓글 내용은 필수입니다")
        @Size(max = 1000, message = "댓글 내용은 1000자를 초과할 수 없습니다")
        String content
) {
}