package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
public record UpdateCategoryRequest(
    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 100, message = "카테고리 이름은 100자를 초과할 수 없습니다")
    String name,

    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    String description,

    @Size(max = 100, message = "슬러그는 100자를 초과할 수 없습니다")
    String slug,

    Boolean showOnHomepage
) {
}