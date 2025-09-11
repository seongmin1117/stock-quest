package com.stockquest.domain.challenge;

/**
 * 챌린지 난이도 열거형
 */
public enum ChallengeDifficulty {
    BEGINNER("초급"),
    INTERMEDIATE("중급"),
    ADVANCED("고급"),
    EXPERT("전문가");
    
    private final String displayName;
    
    ChallengeDifficulty(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}