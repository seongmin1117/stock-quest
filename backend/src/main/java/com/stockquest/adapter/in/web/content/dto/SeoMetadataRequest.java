package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

import jakarta.validation.constraints.Size;
import java.util.List;

@Builder
public record SeoMetadataRequest(
    @Size(max = 200, message = "SEO 제목은 200자를 초과할 수 없습니다")
    String seoTitle,

    @Size(max = 500, message = "메타 설명은 500자를 초과할 수 없습니다")
    String metaDescription,

    List<String> keywords,

    @Size(max = 500, message = "정규 URL은 500자를 초과할 수 없습니다")
    String canonicalUrl
) {
}