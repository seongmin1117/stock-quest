package com.stockquest.domain.content.article;

/**
 * 블로그 글 발행 상태
 */
public enum ArticleStatus {

    /**
     * 초안 - 작성 중이거나 검토 중인 상태
     */
    DRAFT("초안"),

    /**
     * 발행됨 - 공개적으로 접근 가능한 상태
     */
    PUBLISHED("발행됨"),

    /**
     * 보관됨 - 발행 중단되었지만 삭제되지 않은 상태
     */
    ARCHIVED("보관됨"),

    /**
     * 삭제됨 - 소프트 삭제된 상태
     */
    DELETED("삭제됨");

    private final String description;

    ArticleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 공개적으로 접근 가능한 상태인지 확인
     */
    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }

    /**
     * 편집 가능한 상태인지 확인
     */
    public boolean isEditable() {
        return this == DRAFT || this == PUBLISHED;
    }
}