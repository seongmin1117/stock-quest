package com.stockquest.domain.content.article;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SEO 최적화를 위한 메타데이터 값 객체
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SeoMetadata {

    /**
     * SEO 제목 (title 태그에 사용)
     */
    private String seoTitle;

    /**
     * 메타 설명 (meta description)
     */
    private String metaDescription;

    /**
     * SEO 키워드 목록
     */
    private List<String> keywords;

    /**
     * 정규 URL (canonical URL)
     */
    private String canonicalUrl;

    /**
     * Open Graph 제목
     */
    private String ogTitle;

    /**
     * Open Graph 설명
     */
    private String ogDescription;

    /**
     * Open Graph 이미지 URL
     */
    private String ogImageUrl;

    /**
     * Twitter Card 타입
     */
    private String twitterCardType;

    /**
     * Twitter 제목
     */
    private String twitterTitle;

    /**
     * Twitter 설명
     */
    private String twitterDescription;

    /**
     * Twitter 이미지 URL
     */
    private String twitterImageUrl;

    /**
     * 검색 엔진 인덱싱 허용 여부
     */
    @Builder.Default
    private boolean indexable = true;

    /**
     * 팔로우 링크 허용 여부
     */
    @Builder.Default
    private boolean followable = true;

    /**
     * 구조화된 데이터 스키마 타입
     */
    private String schemaType;

    /**
     * Article 도메인으로부터 기본 SEO 메타데이터 생성
     */
    public static SeoMetadata fromArticle(Article article) {
        return SeoMetadata.builder()
                .seoTitle(generateSeoTitle(article.getTitle()))
                .metaDescription(generateMetaDescription(article.getSummary()))
                .ogTitle(article.getTitle())
                .ogDescription(article.getSummary())
                .twitterCardType("summary_large_image")
                .twitterTitle(article.getTitle())
                .twitterDescription(article.getSummary())
                .schemaType("Article")
                .indexable(article.isPublished())
                .followable(true)
                .build();
    }

    /**
     * SEO 친화적 제목 생성
     */
    private static String generateSeoTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "StockQuest - 투자 교육 플랫폼";
        }

        String seoTitle = title;
        if (!title.contains("StockQuest")) {
            seoTitle = title + " | StockQuest";
        }

        // SEO 제목은 60자 이내로 제한
        if (seoTitle.length() > 60) {
            seoTitle = title.substring(0, Math.min(title.length(), 50)) + "... | StockQuest";
        }

        return seoTitle;
    }

    /**
     * SEO 친화적 메타 설명 생성
     */
    private static String generateMetaDescription(String summary) {
        if (summary == null || summary.trim().isEmpty()) {
            return "StockQuest에서 제공하는 실전 투자 교육 컨텐츠를 통해 투자 실력을 향상시키세요.";
        }

        String metaDesc = summary;

        // 메타 설명은 160자 이내로 제한
        if (metaDesc.length() > 160) {
            metaDesc = summary.substring(0, 157) + "...";
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

        if (keywords != null && keywords.size() > 10) {
            throw new IllegalArgumentException("키워드는 최대 10개까지 설정 가능합니다");
        }
    }

    /**
     * robots 메타 태그 값 생성
     */
    public String getRobotsMetaContent() {
        StringBuilder robots = new StringBuilder();

        if (indexable) {
            robots.append("index");
        } else {
            robots.append("noindex");
        }

        robots.append(",");

        if (followable) {
            robots.append("follow");
        } else {
            robots.append("nofollow");
        }

        return robots.toString();
    }

    /**
     * 키워드를 쉼표로 구분된 문자열로 변환
     */
    public String getKeywordsAsString() {
        if (keywords == null || keywords.isEmpty()) {
            return "";
        }
        return String.join(",", keywords);
    }

    /**
     * 완전한 SEO 메타데이터인지 확인
     */
    public boolean isComplete() {
        return seoTitle != null && !seoTitle.trim().isEmpty() &&
               metaDescription != null && !metaDescription.trim().isEmpty() &&
               keywords != null && !keywords.isEmpty();
    }
}