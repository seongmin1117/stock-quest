package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
public record CreateTagRequest(
    @NotBlank(message = "태그 이름은 필수입니다")
    @Size(max = 50, message = "태그 이름은 50자를 초과할 수 없습니다")
    String name
) {
}