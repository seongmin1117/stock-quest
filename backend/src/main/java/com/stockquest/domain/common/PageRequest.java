package com.stockquest.domain.common;

/**
 * 헥사고날 아키텍처를 위한 프레임워크 독립적인 페이지 요청
 * Spring Data의 Pageable을 대체하는 도메인 객체
 */
public class PageRequest {

    private final int page;
    private final int size;
    private final Sort sort;

    public PageRequest(int page, int size) {
        this(page, size, Sort.unsorted());
    }

    public PageRequest(int page, int size, Sort sort) {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        if (size < 1) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다");
        }
        if (sort == null) {
            throw new IllegalArgumentException("정렬 조건은 필수입니다");
        }

        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public Sort getSort() {
        return sort;
    }

    public int getOffset() {
        return page * size;
    }

    public PageRequest next() {
        return new PageRequest(page + 1, size, sort);
    }

    public PageRequest previous() {
        return page == 0 ? this : new PageRequest(page - 1, size, sort);
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    public static PageRequest of(int page, int size, Sort sort) {
        return new PageRequest(page, size, sort);
    }
}