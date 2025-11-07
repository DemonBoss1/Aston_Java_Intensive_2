package com.userservice.application.usecase;

import com.userservice.domain.model.User;
import com.userservice.domain.model.Email;
import com.userservice.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserUseCase deleteUserUseCase;

    @Test
    @DisplayName("Удаление существующего пользователя - успех")
    void execute_WithExistingUser_ShouldDeleteUser() {
        // Given
        Long userId = 1L;
        User existingUser = new User(userId, "To Delete", new Email("delete@example.com"), 25, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).delete(userId);

        // When
        boolean result = deleteUserUseCase.execute(userId);

        // Then
        assertTrue(result);
        verify(userRepository).findById(userId);
        verify(userRepository).delete(userId);
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя - false")
    void execute_WithNonExistingUser_ShouldReturnFalse() {
        // Given
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        boolean result = deleteUserUseCase.execute(userId);

        // Then
        assertFalse(result);
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Удаление пользователя с null ID - исключение")
    void execute_WithNullId_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deleteUserUseCase.execute(null)
        );

        assertEquals("Invalid user ID", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Удаление пользователя с отрицательным ID - исключение")
    void execute_WithNegativeId_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deleteUserUseCase.execute(-1L)
        );

        assertEquals("Invalid user ID", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Удаление пользователя с нулевым ID - исключение")
    void execute_WithZeroId_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deleteUserUseCase.execute(0L)
        );

        assertEquals("Invalid user ID", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).delete(any());
    }
}