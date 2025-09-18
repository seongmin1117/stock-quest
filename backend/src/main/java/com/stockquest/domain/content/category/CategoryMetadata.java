package com.stockquest.domain.content.category;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 카테고리 메타데이터 값 객체
 * SEO 및 추가 설정 정보
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryMetadata {

    /**
     * SEO 제목
     */
    private String seoTitle;

    /**
     * 메타 설명
     */
    private String metaDescription;

    /**
     * SEO 키워드 목록
     */
    private List<String> seoKeywords;

    /**
     * 배너 이미지 URL
     */
    private String bannerImageUrl;

    /**
     * 카테고리 썸네일 이미지 URL
     */
    private String thumbnailImageUrl;

    /**
     * 카테고리 설명 이미지 URL
     */
    private String descriptionImageUrl;

    /**
     * 추천 카테고리 여부
     */
    @Builder.Default
    private boolean featured = false;

    /**
     * 메인 페이지 노출 여부
     */
    @Builder.Default
    private boolean showOnHomepage = false;

    /**
     * 네비게이션 메뉴 노출 여부
     */
    @Builder.Default
    private boolean showInNavigation = true;

    /**
     * 검색 결과 포함 여부
     */
    @Builder.Default
    private boolean includeInSearch = true;

    /**
     * RSS 피드 포함 여부
     */
    @Builder.Default
    private boolean includeInRss = true;

    /**
     * 카테고리별 템플릿명
     */
    private String templateName;

    /**
     * 추가 CSS 클래스
     */
    private String customCssClass;

    /**
     * 외부 링크 URL (카테고리가 외부 링크인 경우)
     */
    private String externalUrl;

    /**
     * 새 탭에서 열기 여부
     */
    @Builder.Default
    private boolean openInNewTab = false;

    /**
     * 카테고리 접근 권한 레벨
     */
    @Builder.Default
    private CategoryAccessLevel accessLevel = CategoryAccessLevel.PUBLIC;

    /**
     * 구조화된 데이터 스키마 타입
     */
    private String schemaType;

    /**
     * 카테고리별 정렬 기본값
     */
    @Builder.Default
    private CategorySortOption defaultSortOption = CategorySortOption.PUBLISHED_DATE_DESC;

    /**
     * Category 도메인으로부터 기본 메타데이터 생성
     */
    public static CategoryMetadata fromCategory(Category category) {
        return CategoryMetadata.builder()
                .seoTitle(generateSeoTitle(category.getName()))
                .metaDescription(generateMetaDescription(category.getName(), category.getDescription()))
                .schemaType("CollectionPage")
                .featured(false)
                .showOnHomepage(category.isRootCategory())
                .showInNavigation(true)
                .includeInSearch(true)
                .includeInRss(true)
                .accessLevel(CategoryAccessLevel.PUBLIC)
                .defaultSortOption(CategorySortOption.PUBLISHED_DATE_DESC)
                .build();
    }

    /**
     * SEO 친화적 제목 생성
     */
    private static String generateSeoTitle(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "StockQuest - 투자 교육";
        }

        String seoTitle = categoryName + " - 투자 교육 | StockQuest";

        // SEO 제목은 60자 이내로 제한
        if (seoTitle.length() > 60) {
            seoTitle = categoryName.substring(0, Math.min(categoryName.length(), 40)) + "... | StockQuest";
        }

        return seoTitle;
    }

    /**
     * SEO 친화적 메타 설명 생성
     */
    private static String generateMetaDescription(String categoryName, String description) {
        String metaDesc;

        if (description != null && !description.trim().isEmpty()) {
            metaDesc = description;
        } else {
            metaDesc = categoryName + " 관련 투자 교육 컨텐츠를 StockQuest에서 확인하세요.";
        }

        // 메타 설명은 160자 이내로 제한
        if (metaDesc.length() > 160) {
            metaDesc = metaDesc.substring(0, 157) + "...";
        }

        return metaDesc;
    }

    /**
     * 메타데이터 검증
     */
    public void validate() {
        if (seoTitle != null && seoTitle.length() > 60) {
            throw new IllegalArgumentException("SEO 제목은 60자를 초과할 수 없습니다");
        }

        if (metaDescription != null && metaDescription.length() > 160) {
            throw new IllegalArgumentException("메타 설명은 160자를 초과할 수 없습니다");
        }

        if (seoKeywords != null && seoKeywords.size() > 10) {
            throw new IllegalArgumentException("SEO 키워드는 최대 10개까지 설정 가능합니다");
        }

        if (externalUrl != null && !externalUrl.trim().isEmpty()) {
            if (!externalUrl.startsWith("http://") && !externalUrl.startsWith("https://")) {
                throw new IllegalArgumentException("외부 링크는 http:// 또는 https://로 시작해야 합니다");
            }
        }
    }

    /**
     * 외부 링크 카테고리인지 확인
     */
    public boolean isExternalLink() {
        return externalUrl != null && !externalUrl.trim().isEmpty();
    }

    /**
     * 공개 카테고리인지 확인
     */
    public boolean isPublic() {
        return accessLevel == CategoryAccessLevel.PUBLIC;
    }

    /**
     * 로그인 필요 카테고리인지 확인
     */
    public boolean requiresLogin() {
        return accessLevel == CategoryAccessLevel.MEMBERS_ONLY ||
               accessLevel == CategoryAccessLevel.PREMIUM_ONLY;
    }

    /**
     * 프리미엄 전용 카테고리인지 확인
     */
    public boolean isPremiumOnly() {
        return accessLevel == CategoryAccessLevel.PREMIUM_ONLY;
    }

    /**
     * 키워드를 쉼표로 구분된 문자열로 변환
     */
    public String getSeoKeywordsAsString() {
        if (seoKeywords == null || seoKeywords.isEmpty()) {
            return "";
        }
        return String.join(",", seoKeywords);
    }

    /**
     * 카테고리 접근 권한 레벨
     */
    public enum CategoryAccessLevel {
        /**
         * 공개 - 모든 사용자 접근 가능
         */
        PUBLIC("공개"),

        /**
         * 회원 전용 - 로그인한 사용자만 접근 가능
         */
        MEMBERS_ONLY("회원 전용"),

        /**
         * 프리미엄 전용 - 프리미엄 구독자만 접근 가능
         */
        PREMIUM_ONLY("프리미엄 전용"),

        /**
         * 관리자 전용 - 관리자만 접근 가능
         */
        ADMIN_ONLY("관리자 전용");

        private final String displayName;

        CategoryAccessLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 카테고리별 기본 정렬 옵션
     */
    public enum CategorySortOption {
        /**
         * 발행일 내림차순 (최신순)
         */
        PUBLISHED_DATE_DESC("최신순"),

        /**
         * 발행일 오름차순 (오래된순)
         */
        PUBLISHED_DATE_ASC("오래된순"),

        /**
         * 조회수 내림차순 (인기순)
         */
        VIEW_COUNT_DESC("인기순"),

        /**
         * 좋아요 내림차순 (추천순)
         */
        LIKE_COUNT_DESC("추천순"),

        /**
         * 제목 오름차순 (가나다순)
         */
        TITLE_ASC("제목순"),

        /**
         * 난이도 오름차순 (쉬운순)
         */
        DIFFICULTY_ASC("쉬운순"),

        /**
         * 난이도 내림차순 (어려운순)
         */
        DIFFICULTY_DESC("어려운순"),

        /**
         * 읽기 시간 오름차순 (짧은순)
         */
        READING_TIME_ASC("짧은순"),

        /**
         * 읽기 시간 내림차순 (긴순)
         */
        READING_TIME_DESC("긴순");

        private final String displayName;

        CategorySortOption(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}