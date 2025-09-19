package com.stockquest.adapter.in.web.content.dto;

import com.stockquest.domain.content.article.ArticleDifficulty;
import lombok.Builder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Builder
public record UpdateArticleRequest(
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    String title,

    @NotBlank(message = "내용은 필수입니다")
    String content,

    @Size(max = 500, message = "요약은 500자를 초과할 수 없습니다")
    String summary,

    @NotNull(message = "카테고리 ID는 필수입니다")
    Long categoryId,

    @NotNull(message = "난이도는 필수입니다")
    ArticleDifficulty difficulty,

    Boolean featured,

    List<Long> tagIds,

    @Valid
    SeoMetadataRequest seoMetadata
) {
}