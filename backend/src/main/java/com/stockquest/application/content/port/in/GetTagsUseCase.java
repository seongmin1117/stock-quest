package com.stockquest.application.content.port.in;

import com.stockquest.application.content.dto.TagDto;
import com.stockquest.domain.content.tag.TagType;

import java.util.List;
import java.util.Optional;

/**
 * 태그 조회 Use Case (입력 포트)
 */
public interface GetTagsUseCase {

    /**
     * 인기 태그 조회
     *
     * @param limit 조회할 태그 수
     * @return 인기 태그 목록
     */
    List<TagDto> getPopularTags(int limit);

    /**
     * 타입별 태그 조회
     *
     * @param type 태그 타입
     * @return 해당 타입의 활성 태그 목록
     */
    List<TagDto> getTagsByType(TagType type);

    /**
     * ID로 태그 조회
     *
     * @param id 태그 ID
     * @return 태그 정보
     */
    Optional<TagDto> getTagById(Long id);

    /**
     * 이름으로 태그 조회
     *
     * @param name 태그 이름
     * @return 태그 정보
     */
    Optional<TagDto> getTagByName(String name);

    /**
     * 태그 검색 (자동완성용)
     *
     * @param keyword 검색 키워드
     * @param limit 결과 수 제한
     * @return 검색된 태그 목록
     */
    List<TagDto> searchTags(String keyword, int limit);

    /**
     * 글의 태그 목록 조회
     *
     * @param articleId 글 ID
     * @return 태그 목록
     */
    List<TagDto> getTagsByArticleId(Long articleId);
}