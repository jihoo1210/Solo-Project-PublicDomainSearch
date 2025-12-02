package com.example.backend.service;

import com.example.backend.dto.auth.AuthMyInfoResponse;
import com.example.backend.entity.user.User;
import com.example.backend.entity.user.enumeration.AuthProvider;
import com.example.backend.entity.user.enumeration.Role;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserService 테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.ROLE_USER)
                .build();
    }

    @DisplayName("새로운 사용자 생성 - 기존 사용자가 없을 때")
    @Test
    void testCreateUserSuccess() {
        // given
        String email = "newuser@example.com";
        String username = "newuser";
        String password = "encodedPassword";
        AuthProvider authProvider = AuthProvider.LOCAL;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        User result = userService.createUser(email, username, password, authProvider);

        // then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @DisplayName("사용자 생성 - 이미 존재하는 이메일")
    @Test
    void testCreateUserAlreadyExists() {
        // given
        String email = "test@example.com";
        String username = "testuser";
        String password = "encodedPassword";
        AuthProvider authProvider = AuthProvider.LOCAL;

        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when
        User result = userService.createUser(email, username, password, authProvider);

        // then
        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @DisplayName("인증 제공자 확인 - 일치할 때")
    @Test
    void testCheckAuthProviderMatches() {
        // given
        String email = "test@example.com";
        AuthProvider authProvider = AuthProvider.LOCAL;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when
        boolean result = userService.checkAuthProvider(email, authProvider);

        // then
        assertTrue(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @DisplayName("인증 제공자 확인 - 일치하지 않을 때")
    @Test
    void testCheckAuthProviderNotMatches() {
        // given
        String email = "test@example.com";
        AuthProvider authProvider = AuthProvider.NAVER;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when
        boolean result = userService.checkAuthProvider(email, authProvider);

        // then
        assertFalse(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @DisplayName("인증 제공자 확인 - 사용자가 없을 때")
    @Test
    void testCheckAuthProviderUserNotFound() {
        // given
        String email = "nonexistent@example.com";
        AuthProvider authProvider = AuthProvider.LOCAL;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        boolean result = userService.checkAuthProvider(email, authProvider);

        // then
        assertFalse(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @DisplayName("이메일로 사용자 정보 조회 - 성공")
    @Test
    void testGetUserByEmailSuccess() {
        // given
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when
        AuthMyInfoResponse result = userService.getUserByEmail(email);

        // then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
        assertEquals(Role.ROLE_USER, result.getRole());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @DisplayName("이메일로 사용자 정보 조회 - 사용자가 없을 때")
    @Test
    void testGetUserByEmailNotFound() {
        // given
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        AuthMyInfoResponse result = userService.getUserByEmail(email);

        // then
        assertNull(result);
        verify(userRepository, times(1)).findByEmail(email);
    }
}

