package com.stockquest.domain.content.tag.port;

import com.stockquest.domain.content.tag.Tag;
import com.stockquest.domain.content.tag.TagType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 태그 저장소 포트 (출력 포트)
 */
public interface TagRepository {

    /**
     * 태그 저장
     */
    Tag save(Tag tag);

    /**
     * ID로 태그 조회
     */
    Optional<Tag> findById(Long id);

    /**
     * 이름으로 태그 조회
     */
    Optional<Tag> findByName(String name);

    /**
     * slug로 태그 조회
     */
    Optional<Tag> findBySlug(String slug);

    /**
     * 모든 활성화된 태그 조회
     */
    List<Tag> findAllActive();

    /**
     * 태그 타입별 조회
     */
    List<Tag> findByType(TagType type);

    /**
     * 활성화된 태그를 타입별로 조회
     */
    List<Tag> findActiveByType(TagType type);

    /**
     * 인기 태그 목록 조회 (사용 횟수 순)
     */
    List<Tag> findPopularTags(int limit);

    /**
     * 추천 태그 목록 조회
     */
    List<Tag> findSuggestedTags();

    /**
     * 최근 생성된 태그 목록 조회
     */
    List<Tag> findRecentlyCreated(int limit);

    /**
     * 사용 중인 태그 목록 조회 (사용 횟수 > 0)
     */
    List<Tag> findTagsInUse();

    /**
     * 사용되지 않는 태그 목록 조회 (사용 횟수 = 0)
     */
    List<Tag> findUnusedTags();

    /**
     * 특정 글의 태그 목록 조회
     */
    List<Tag> findByArticleId(Long articleId);

    /**
     * 여러 글의 태그 목록 조회 (중복 제거)
     */
    List<Tag> findByArticleIds(List<Long> articleIds);

    /**
     * 관련 태그 목록 조회 (같은 글에서 함께 사용된 태그)
     */
    List<Tag> findRelatedTags(Long tagId, int limit);

    /**
     * 태그 이름으로 검색 (부분 일치)
     */
    List<Tag> searchByName(String keyword);

    /**
     * 태그 자동완성 검색 (이름 시작 부분 일치)
     */
    List<Tag> findByNameStartingWith(String prefix, int limit);

    /**
     * 태그 타입별 사용 횟수 통계
     */
    List<TagTypeStats> getTagTypeStats();

    /**
     * 일별 태그 사용 통계
     */
    List<TagUsageStats> getDailyTagUsageStats(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 트렌딩 태그 조회 (최근 N일간 사용 급증한 태그)
     */
    List<Tag> findTrendingTags(int days, int limit);

    /**
     * 가중치 기준 상위 태그 조회
     */
    List<Tag> findTopWeightedTags(int limit);

    /**
     * 특정 기간 내 생성된 태그 수 조회
     */
    long countCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 활성화된 태그 수 조회
     */
    long countActive();

    /**
     * 태그 타입별 태그 수 조회
     */
    long countByType(TagType type);

    /**
     * 사용 중인 태그 수 조회
     */
    long countInUse();

    /**
     * 태그 이름 중복 확인
     */
    boolean existsByName(String name);

    /**
     * slug 중복 확인
     */
    boolean existsBySlug(String slug);

    /**
     * 태그 삭제 (하드 삭제)
     */
    void deleteById(Long id);

    /**
     * 사용되지 않는 태그 일괄 삭제
     */
    void deleteUnusedTags();

    /**
     * 태그 사용 횟수 업데이트 (배치 처리용)
     */
    void updateUsageCount(Long tagId, Long usageCount);

    /**
     * 모든 태그의 사용 횟수 재계산
     */
    void recalculateAllUsageCounts();

    /**
     * 인기 태그 상태 일괄 업데이트
     */
    void updatePopularStatus(int threshold);

    /**
     * 태그 가중치 일괄 업데이트
     */
    void updateWeights(List<TagWeightUpdate> updates);

    /**
     * 태그 타입별 통계 데이터 클래스
     */
    record TagTypeStats(
        TagType type,
        String typeName,
        Long tagCount,
        Long totalUsage,
        Double averageUsage,
        Long activeTagCount
    ) {}

    /**
     * 태그 사용 통계 데이터 클래스
     */
    record TagUsageStats(
        LocalDateTime date,
        Long tagId,
        String tagName,
        Long usageCount,
        Long newUsages
    ) {}

    /**
     * 태그 가중치 업데이트 데이터 클래스
     */
    record TagWeightUpdate(
        Long tagId,
        Double weight
    ) {}

    /**
     * 태그 클라우드 데이터 조회 (이름, 사용횟수, 가중치)
     */
    List<TagCloudData> getTagCloudData(int limit);

    /**
     * 태그 클라우드 데이터 클래스
     */
    record TagCloudData(
        Long tagId,
        String tagName,
        String tagSlug,
        TagType tagType,
        String colorCode,
        Long usageCount,
        Double weight,
        Double popularityScore
    ) {}

    /**
     * 태그 공동 출현 분석 (함께 사용되는 태그 조합)
     */
    List<TagCooccurrence> getTagCooccurrences(Long tagId, int limit);

    /**
     * 태그 공동 출현 데이터 클래스
     */
    record TagCooccurrence(
        Long tagId,
        String tagName,
        Long cooccurrenceCount,
        Double correlationScore
    ) {}

    /**
     * 태그별 글 수 조회
     */
    List<TagArticleCount> getTagArticleCounts();

    /**
     * 태그별 글 수 데이터 클래스
     */
    record TagArticleCount(
        Long tagId,
        String tagName,
        Long articleCount,
        Long publishedArticleCount
    ) {}

    /**
     * 시간대별 태그 사용 패턴 분석
     */
    List<TagUsagePattern> getTagUsagePatterns(Long tagId);

    /**
     * 태그 사용 패턴 데이터 클래스
     */
    record TagUsagePattern(
        Integer hour,
        Integer dayOfWeek,
        Long usageCount,
        Double intensity
    ) {}

    /**
     * 인기하고 활성화된 태그 목록 조회
     */
    default List<Tag> findPopularActive(int limit) {
        return findPopularTags(limit).stream()
                .filter(Tag::isActive)
                .limit(limit)
                .toList();
    }

    /**
     * 타입과 활성화 상태로 태그 조회
     */
    default List<Tag> findByTypeAndActive(TagType type, boolean active) {
        return findByType(type).stream()
                .filter(tag -> tag.isActive() == active)
                .toList();
    }
}