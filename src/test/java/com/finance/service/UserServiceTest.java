package com.finance.service;

import com.finance.dto.user.CreateUserRequest;
import com.finance.dto.user.UpdateUserRequest;
import com.finance.dto.user.UserResponse;
import com.finance.entity.Role;
import com.finance.entity.Status;
import com.finance.entity.User;
import com.finance.exception.DuplicateResourceException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.mapper.UserMapper;
import com.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encoded")
                .role(Role.ANALYST)
                .status(Status.ACTIVE)
                .build();

        testResponse = UserResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .role("ANALYST")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUser() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .password("password123")
                    .role("ANALYST")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(testUser)).thenReturn(testResponse);

            UserResponse result = userService.createUser(request);

            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getRole()).isEqualTo("ANALYST");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw on duplicate email")
        void shouldThrowOnDuplicate() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("Test").email("test@example.com").password("pass").role("VIEWER").build();

            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Get Users")
    class GetUserTests {

        @Test
        @DisplayName("Should return all users")
        void shouldReturnAllUsers() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testResponse);

            List<UserResponse> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return user by ID")
        void shouldReturnById() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testResponse);

            UserResponse result = userService.getUserById(1L);
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUser() {
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("Updated Name")
                    .email("test@example.com")
                    .role("ADMIN")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            UserResponse result = userService.updateUser(1L, request);
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Update Status")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should activate/deactivate user")
        void shouldUpdateStatus() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            UserResponse result = userService.updateUserStatus(1L, "INACTIVE");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw on invalid status")
        void shouldThrowOnInvalidStatus() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> userService.updateUserStatus(1L, "INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            userService.deleteUser(1L);
            verify(userRepository).delete(testUser);
        }
    }
}
