package com.userservice.application.usecase;

import com.userservice.domain.model.User;
import com.userservice.domain.model.Email;
import com.userservice.domain.repository.UserRepository;
import com.userservice.application.dto.UpdateUserRequest;
import com.userservice.application.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    @Test
    @DisplayName("Обновление пользователя с валидными данными - успех")
    void execute_WithValidData_ShouldUpdateUser() {
        // Given
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", new Email("old@example.com"), 25, null);
        UpdateUserRequest request = new UpdateUserRequest(userId, "New Name", "new@example.com", 30);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        doNothing().when(userRepository).update(any(User.class));

        // When
        UserResponse response = updateUserUseCase.execute(request);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("New Name", response.getName());
        assertEquals("new@example.com", response.getEmail());
        assertEquals(30, response.getAge());

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(new Email("new@example.com"));
        verify(userRepository).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя без изменения email - успех")
    void execute_WithSameEmail_ShouldUpdateUser() {
        // Given
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", new Email("same@example.com"), 25, null);
        UpdateUserRequest request = new UpdateUserRequest(userId, "New Name", "same@example.com", 30);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).update(any(User.class));

        // When
        UserResponse response = updateUserUseCase.execute(request);

        // Then
        assertEquals("New Name", response.getName());
        assertEquals("same@example.com", response.getEmail());

        verify(userRepository, never()).existsByEmail(any(Email.class));
        verify(userRepository).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя с дублирующимся email - исключение")
    void execute_WithDuplicateEmail_ShouldThrowException() {
        // Given
        Long userId = 1L;
        User existingUser = new User(userId, "Existing User", new Email("old@example.com"), 25, null);
        UpdateUserRequest request = new UpdateUserRequest(userId, "New Name", "duplicate@example.com", 30);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateUserUseCase.execute(request)
        );

        assertEquals("User with this email already exists", exception.getMessage());

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(new Email("duplicate@example.com"));
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя - исключение")
    void execute_WithNonExistingUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        UpdateUserRequest request = new UpdateUserRequest(userId, "New Name", "new@example.com", 30);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateUserUseCase.execute(request)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());

        verify(userRepository).findById(userId);
        verify(userRepository, never()).existsByEmail(any(Email.class));
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя с null ID - исключение")
    void execute_WithNullId_ShouldThrowException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(null, "Name", "email@example.com", 25);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateUserUseCase.execute(request)
        );

        assertEquals("Invalid user ID", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя с отрицательным ID - исключение")
    void execute_WithNegativeId_ShouldThrowException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(-1L, "Name", "email@example.com", 25);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateUserUseCase.execute(request)
        );

        assertEquals("Invalid user ID", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя без возраста - успех")
    void execute_WithNullAge_ShouldUpdateUser() {
        // Given
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", new Email("old@example.com"), 25, null);
        UpdateUserRequest request = new UpdateUserRequest(userId, "New Name", "new@example.com", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        doNothing().when(userRepository).update(any(User.class));

        // When
        UserResponse response = updateUserUseCase.execute(request);

        // Then
        assertNull(response.getAge());
        verify(userRepository).update(any(User.class));
    }
}