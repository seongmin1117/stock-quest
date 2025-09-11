package com.stockquest.domain.challenge;

/**
 * 챌린지 상태
 */
public enum ChallengeStatus {
    DRAFT("초안"),          // 생성됨, 아직 활성화되지 않음
    SCHEDULED("예약됨"),     // 시작 일정이 예약됨
    ACTIVE("진행중"),        // 활성화됨, 사용자 참여 가능
    COMPLETED("완료"),       // 완료됨, 결과 공개
    ARCHIVED("보관됨"),      // 보관됨, 참조용
    CANCELLED("취소됨");     // 취소됨
    
    private final String description;
    
    ChallengeStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}