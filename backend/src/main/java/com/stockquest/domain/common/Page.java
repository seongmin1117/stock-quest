package com.stockquest.domain.common;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 헥사고날 아키텍처를 위한 프레임워크 독립적인 페이지 결과
 * Spring Data의 Page를 대체하는 도메인 객체
 */
public class Page<T> {

    private final List<T> content;
    private final PageRequest pageRequest;
    private final long totalElements;

    public Page(List<T> content, PageRequest pageRequest, long totalElements) {
        if (content == null) {
            throw new IllegalArgumentException("내용은 필수입니다");
        }
        if (pageRequest == null) {
            throw new IllegalArgumentException("페이지 요청은 필수입니다");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("전체 요소 수는 0 이상이어야 합니다");
        }

        this.content = Collections.unmodifiableList(content);
        this.pageRequest = pageRequest;
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageRequest.getSize());
    }

    public int getSize() {
        return pageRequest.getSize();
    }

    public int getNumber() {
        return pageRequest.getPage();
    }

    public int getNumberOfElements() {
        return content.size();
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public boolean isFirst() {
        return !hasPrevious();
    }

    public boolean isLast() {
        return !hasNext();
    }

    public boolean hasNext() {
        return getNumber() + 1 < getTotalPages();
    }

    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    public PageRequest nextPageable() {
        return hasNext() ? pageRequest.next() : null;
    }

    public PageRequest previousPageable() {
        return hasPrevious() ? pageRequest.previous() : null;
    }

    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        List<U> convertedContent = content.stream()
                .map(converter)
                .collect(Collectors.toList());
        return new Page<>(convertedContent, pageRequest, totalElements);
    }

    public static <T> Page<T> empty() {
        return new Page<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
    }

    public static <T> Page<T> empty(PageRequest pageRequest) {
        return new Page<>(Collections.emptyList(), pageRequest, 0);
    }
}