package com.userservice.application.usecase;

import com.userservice.domain.model.User;
import com.userservice.domain.model.Email;
import com.userservice.domain.repository.UserRepository;
import com.userservice.application.dto.UserResponse;
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
class GetUserByIdUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserByIdUseCase getUserByIdUseCase;

    @Test
    @DisplayName("Поиск пользователя по существующему ID")
    void execute_WithExistingId_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        User user = new User(userId, "John Doe", new Email("john@example.com"), 30, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        Optional<UserResponse> result = getUserByIdUseCase.execute(userId);

        // Then
        assertTrue(result.isPresent());
        UserResponse response = result.get();
        assertEquals(userId, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(30, response.getAge());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Поиск пользователя по несуществующему ID")
    void execute_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<UserResponse> result = getUserByIdUseCase.execute(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Поиск пользователя с null ID - должно бросить исключение")
    void execute_WithNullId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                getUserByIdUseCase.execute(null)
        );
    }

    @Test
    @DisplayName("Поиск пользователя с отрицательным ID - должно бросить исключение")
    void execute_WithNegativeId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                getUserByIdUseCase.execute(-1L)
        );
    }
}
