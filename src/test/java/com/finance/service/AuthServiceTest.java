package com.finance.service;

import com.finance.dto.auth.AuthResponse;
import com.finance.dto.auth.LoginRequest;
import com.finance.dto.auth.RegisterRequest;
import com.finance.entity.Role;
import com.finance.entity.Status;
import com.finance.entity.User;
import com.finance.exception.DuplicateResourceException;
import com.finance.repository.UserRepository;
import com.finance.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.VIEWER)
                .status(Status.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Register")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new user successfully")
        void shouldRegisterSuccessfully() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .password("password123")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt_token");

            AuthResponse response = authService.register(request);

            assertThat(response.getToken()).isEqualTo("jwt_token");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getName()).isEqualTo("Test User");
            assertThat(response.getRole()).isEqualTo("VIEWER");

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate email")
        void shouldThrowOnDuplicateEmail() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .password("password123")
                    .build();

            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("test@example.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should assign specified role during registration")
        void shouldAssignSpecifiedRole() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .password("password123")
                    .role("ADMIN")
                    .build();

            User adminUser = User.builder()
                    .id(2L).name("Admin User").email("admin@example.com")
                    .password("encoded").role(Role.ADMIN).status(Status.ACTIVE)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenReturn(adminUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("token");

            AuthResponse response = authService.register(request);
            assertThat(response.getRole()).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(testUser)).thenReturn("jwt_token");

            AuthResponse response = authService.login(request);

            assertThat(response.getToken()).isEqualTo("jwt_token");
            assertThat(response.getEmail()).isEqualTo("test@example.com");

            verify(authenticationManager).authenticate(
                    any(UsernamePasswordAuthenticationToken.class));
        }
    }
}
