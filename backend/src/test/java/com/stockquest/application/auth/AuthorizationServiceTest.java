package com.stockquest.application.auth;

import com.stockquest.domain.user.Role;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationService 테스트")
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .passwordHash("hashedPassword")
                .nickname("관리자")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        normalUser = User.builder()
                .id(2L)
                .email("user@example.com")
                .passwordHash("hashedPassword")
                .nickname("일반사용자")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("관리자는 admin 권한을 가져야 한다")
    void adminShouldHaveAdminRole() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        // when
        boolean result = authorizationService.isAdmin(1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("일반 사용자는 admin 권한을 가지지 않아야 한다")
    void normalUserShouldNotHaveAdminRole() {
        // given
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        // when
        boolean result = authorizationService.isAdmin(2L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 admin 권한을 가지지 않아야 한다")
    void nonExistentUserShouldNotHaveAdminRole() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        boolean result = authorizationService.isAdmin(999L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("null userId는 admin 권한을 가지지 않아야 한다")
    void nullUserIdShouldNotHaveAdminRole() {
        // when
        boolean result = authorizationService.isAdmin(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("리소스 소유자는 자신의 리소스에 접근할 수 있어야 한다")
    void resourceOwnerShouldAccessOwnResource() {
        // when
        boolean result = authorizationService.canAccessResource(1L, 1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 리소스에 접근할 수 있어야 한다")
    void adminShouldAccessOtherUsersResource() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        // when
        boolean result = authorizationService.canAccessResource(1L, 2L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("일반 사용자는 다른 사용자의 리소스에 접근할 수 없어야 한다")
    void normalUserShouldNotAccessOtherUsersResource() {
        // given
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        // when
        boolean result = authorizationService.canAccessResource(2L, 1L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("챌린지 생성자는 자신의 챌린지를 수정할 수 있어야 한다")
    void challengeCreatorShouldModifyOwnChallenge() {
        // when
        boolean result = authorizationService.canModifyChallenge(1L, 1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 챌린지를 수정할 수 있어야 한다")
    void adminShouldModifyAnyChallenge() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        // when
        boolean result = authorizationService.canModifyChallenge(1L, 2L);

        // then
        assertThat(result).isTrue();
    }
}