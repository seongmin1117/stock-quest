package com.stockquest.domain.content.category;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 블로그 카테고리 도메인 엔티티
 * 계층 구조를 지원하는 카테고리 시스템
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Category {

    private Long id;

    /**
     * 카테고리 이름
     */
    private String name;

    /**
     * URL slug (SEO 친화적 URL)
     */
    private String slug;

    /**
     * 카테고리 설명
     */
    private String description;

    /**
     * 부모 카테고리 ID (null이면 최상위 카테고리)
     */
    private Long parentId;

    /**
     * 정렬 순서
     */
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 카테고리 색상 (Hex 코드)
     */
    private String colorCode;

    /**
     * 카테고리 아이콘 (Material-UI 아이콘명 또는 이모지)
     */
    private String icon;

    /**
     * 활성화 여부
     */
    @Builder.Default
    private boolean active = true;

    /**
     * 이 카테고리의 글 수 (캐시된 값)
     */
    @Builder.Default
    private Long articleCount = 0L;

    /**
     * 메타데이터 (SEO, 추가 설정 등)
     */
    private CategoryMetadata metadata;

    /**
     * 생성일
     */
    private LocalDateTime createdAt;

    /**
     * 수정일
     */
    private LocalDateTime updatedAt;

    /**
     * 도메인 생성자
     */
    public Category(String name, String description, Long parentId, String colorCode, String icon) {
        validateName(name);
        validateDescription(description);
        validateColorCode(colorCode);

        this.name = name;
        this.slug = generateSlug(name);
        this.description = description;
        this.parentId = parentId;
        this.colorCode = colorCode;
        this.icon = icon;
        this.sortOrder = 0;
        this.active = true;
        this.articleCount = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 이름은 필수입니다");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("카테고리 이름은 50자를 초과할 수 없습니다");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("카테고리 설명은 500자를 초과할 수 없습니다");
        }
    }

    private void validateColorCode(String colorCode) {
        if (colorCode != null && !colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("색상 코드는 #RRGGBB 형식이어야 합니다");
        }
    }

    /**
     * 이름으로부터 URL slug 생성
     */
    private String generateSlug(String name) {
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9가-힣\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }

    /**
     * 카테고리 정보 수정
     */
    public void update(String name, String description, String colorCode, String icon) {
        validateName(name);
        validateDescription(description);
        validateColorCode(colorCode);

        this.name = name;
        this.slug = generateSlug(name);
        this.description = description;
        this.colorCode = colorCode;
        this.icon = icon;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 부모 카테고리 변경
     */
    public void changeParent(Long newParentId) {
        // 자기 자신을 부모로 설정하는 것을 방지
        if (newParentId != null && newParentId.equals(this.id)) {
            throw new IllegalArgumentException("자기 자신을 부모 카테고리로 설정할 수 없습니다");
        }

        this.parentId = newParentId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 정렬 순서 변경
     */
    public void changeSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            throw new IllegalArgumentException("정렬 순서는 0 이상이어야 합니다");
        }

        this.sortOrder = sortOrder;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 카테고리 활성화/비활성화
     */
    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 글 수 업데이트
     */
    public void updateArticleCount(Long articleCount) {
        if (articleCount == null || articleCount < 0) {
            throw new IllegalArgumentException("글 수는 0 이상이어야 합니다");
        }

        this.articleCount = articleCount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 글 수 증가
     */
    public void incrementArticleCount() {
        this.articleCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 글 수 감소
     */
    public void decrementArticleCount() {
        if (this.articleCount > 0) {
            this.articleCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 메타데이터 설정
     */
    public void setMetadata(CategoryMetadata metadata) {
        this.metadata = metadata;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 최상위 카테고리인지 확인
     */
    public boolean isRootCategory() {
        return this.parentId == null;
    }

    /**
     * 하위 카테고리인지 확인
     */
    public boolean isSubCategory() {
        return this.parentId != null;
    }

    /**
     * 활성화된 카테고리인지 확인
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * 글이 있는 카테고리인지 확인
     */
    public boolean hasArticles() {
        return this.articleCount > 0;
    }

    /**
     * 카테고리 경로 생성 (부모 카테고리 포함)
     */
    public String getFullPath(List<Category> parentCategories) {
        if (parentCategories == null || parentCategories.isEmpty()) {
            return this.name;
        }

        List<String> pathParts = new ArrayList<>();
        parentCategories.forEach(parent -> pathParts.add(parent.getName()));
        pathParts.add(this.name);

        return String.join(" > ", pathParts);
    }

    /**
     * 카테고리 URL 경로 생성
     */
    public String getUrlPath(List<Category> parentCategories) {
        if (parentCategories == null || parentCategories.isEmpty()) {
            return this.slug;
        }

        List<String> pathParts = new ArrayList<>();
        parentCategories.forEach(parent -> pathParts.add(parent.getSlug()));
        pathParts.add(this.slug);

        return String.join("/", pathParts);
    }

    /**
     * 깊이 레벨 계산
     */
    public int getDepthLevel(List<Category> parentCategories) {
        return parentCategories == null ? 0 : parentCategories.size();
    }

    /**
     * 기본 색상 코드 반환 (설정되지 않은 경우)
     */
    public String getEffectiveColorCode() {
        if (this.colorCode != null && !this.colorCode.trim().isEmpty()) {
            return this.colorCode;
        }

        // 깊이에 따른 기본 색상
        return switch (getDepthLevel(null)) {
            case 0 -> "#2196F3"; // 파란색 (최상위)
            case 1 -> "#4CAF50"; // 초록색 (1단계)
            case 2 -> "#FF9800"; // 주황색 (2단계)
            default -> "#9E9E9E"; // 회색 (3단계 이상)
        };
    }

    /**
     * 기본 아이콘 반환 (설정되지 않은 경우)
     */
    public String getEffectiveIcon() {
        if (this.icon != null && !this.icon.trim().isEmpty()) {
            return this.icon;
        }

        // 카테고리명에 따른 기본 아이콘
        String lowerName = this.name.toLowerCase();
        if (lowerName.contains("주식") || lowerName.contains("stock")) {
            return "📈";
        } else if (lowerName.contains("부동산") || lowerName.contains("real")) {
            return "🏠";
        } else if (lowerName.contains("채권") || lowerName.contains("bond")) {
            return "📜";
        } else if (lowerName.contains("펀드") || lowerName.contains("fund")) {
            return "💼";
        } else if (lowerName.contains("암호화폐") || lowerName.contains("crypto")) {
            return "💰";
        } else {
            return "📚";
        }
    }
}