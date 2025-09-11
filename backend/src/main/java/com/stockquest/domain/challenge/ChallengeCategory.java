package com.stockquest.domain.challenge;


import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지 카테고리 도메인 엔티티
 * 챌린지를 주제별로 분류하고 관리하기 위한 카테고리
 */


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeCategory {
    // Getter/Setter
    private Long id;
    private String name;
    private String description;
    private String colorCode;  // UI에서 사용할 색상 코드
    private String iconName;   // UI에서 사용할 아이콘 이름
    private int displayOrder;  // 화면 표시 순서
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



    // 비즈니스 메서드
    public void updateInfo(String name, String description, String colorCode,
                          String iconName, int displayOrder) {
        validateName(name);
        this.name = name;
        this.description = description;
        this.colorCode = colorCode;
        this.iconName = iconName;
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canBeDeleted() {
        // 실제 구현에서는 이 카테고리를 사용하는 챌린지가 있는지 확인해야 함
        return isActive;
    }

    // 검증 메서드
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 이름은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("카테고리 이름은 100자를 초과할 수 없습니다.");
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeCategory that = (ChallengeCategory) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "ChallengeCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", colorCode='" + colorCode + '\'' +
                ", iconName='" + iconName + '\'' +
                ", displayOrder=" + displayOrder +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}