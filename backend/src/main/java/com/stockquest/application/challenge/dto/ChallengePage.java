package com.stockquest.application.challenge.dto;

import com.stockquest.domain.challenge.Challenge;

import java.util.List;

/**
 * 챌린지 페이징 결과 객체
 */
public class ChallengePage {
    private List<Challenge> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;

    // 기본 생성자
    public ChallengePage() {}

    // 전체 생성자
    public ChallengePage(List<Challenge> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 0;
        this.last = page == totalPages - 1;
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }

    // 빌더 패턴을 위한 정적 메서드
    public static ChallengePageBuilder builder() {
        return new ChallengePageBuilder();
    }

    // 빈 페이지 생성을 위한 정적 메서드
    public static ChallengePage empty() {
        return new ChallengePage(List.of(), 0, 0, 0);
    }

    // Getter methods
    public List<Challenge> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    // Setter methods
    public void setContent(List<Challenge> content) {
        this.content = content;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 0;
        this.last = page == totalPages - 1;
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    // Builder class
    public static class ChallengePageBuilder {
        private List<Challenge> content;
        private int page;
        private int size;
        private long totalElements;

        public ChallengePageBuilder content(List<Challenge> content) {
            this.content = content;
            return this;
        }

        public ChallengePageBuilder page(int page) {
            this.page = page;
            return this;
        }

        public ChallengePageBuilder size(int size) {
            this.size = size;
            return this;
        }

        public ChallengePageBuilder totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public ChallengePage build() {
            return new ChallengePage(content, page, size, totalElements);
        }
    }
}