package com.stockquest.application.content.dto;

import lombok.Builder;

/**
 * Command for creating new tag
 */
@Builder
public record CreateTagCommand(
    String name
) {
}