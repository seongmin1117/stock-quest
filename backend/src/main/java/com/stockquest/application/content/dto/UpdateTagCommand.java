package com.stockquest.application.content.dto;

import lombok.Builder;

/**
 * Command for updating existing tag
 */
@Builder
public record UpdateTagCommand(
    String name
) {
}