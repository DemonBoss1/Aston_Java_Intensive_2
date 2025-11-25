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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserByEmailUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserByEmailUseCase getUserByEmailUseCase;

    @Test
    @DisplayName("Поиск пользователя по существующему email - успех")
    void execute_WithExistingEmail_ShouldReturnUser() {
        // Given
        String email = "existing@example.com";
        User user = new User(1L, "John Doe", new Email(email), 30, null);

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));

        // When
        Optional<UserResponse> result = getUserByEmailUseCase.execute(email);

        // Then
        assertTrue(result.isPresent());
        UserResponse response = result.get();
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals(email, response.getEmail());
        assertEquals(30, response.getAge());

        verify(userRepository).findByEmail(new Email(email));
    }

    @Test
    @DisplayName("Поиск пользователя по несуществующему email - пустой результат")
    void execute_WithNonExistingEmail_ShouldReturnEmpty() {
        // Given
        String email = "nonexisting@example.com";

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        // When
        Optional<UserResponse> result = getUserByEmailUseCase.execute(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(new Email(email));
    }

    @Test
    @DisplayName("Поиск пользователя с null email - исключение")
    void execute_WithNullEmail_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                getUserByEmailUseCase.execute(null)
        );

        assertEquals("Email cannot be empty", exception.getMessage());
        verify(userRepository, never()).findByEmail(any(Email.class));
    }

    @Test
    @DisplayName("Поиск пользователя с пустым email - исключение")
    void execute_WithEmptyEmail_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                getUserByEmailUseCase.execute("   ")
        );

        assertEquals("Email cannot be empty", exception.getMessage());
        verify(userRepository, never()).findByEmail(any(Email.class));
    }

    @Test
    @DisplayName("Поиск пользователя с невалидным email - исключение")
    void execute_WithInvalidEmail_ShouldThrowException() {
        // Given
        String invalidEmail = "invalid-email";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                getUserByEmailUseCase.execute(invalidEmail)
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
        verify(userRepository, never()).findByEmail(any(Email.class));
    }

    @Test
    @DisplayName("Поиск пользователя без возраста - успех")
    void execute_WithUserWithoutAge_ShouldReturnUser() {
        // Given
        String email = "noage@example.com";
        User user = new User(1L, "No Age User", new Email(email), null, null);

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));

        // When
        Optional<UserResponse> result = getUserByEmailUseCase.execute(email);

        // Then
        assertTrue(result.isPresent());
        assertNull(result.get().getAge());
        verify(userRepository).findByEmail(new Email(email));
    }
}
