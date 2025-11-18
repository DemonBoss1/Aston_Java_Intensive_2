package com.userservice.application.usecase;

import com.userservice.domain.model.User;
import com.userservice.domain.model.Email;
import com.userservice.domain.repository.UserRepository;
import com.userservice.application.dto.CreateUserRequest;
import com.userservice.application.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    void execute_WithValidData_ShouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john.doe@example.com", 30);
        User savedUser = new User(1L, "John Doe", new Email("john.doe@example.com"), 30, null);

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse response = createUserUseCase.execute(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals(30, response.getAge());

        verify(userRepository).existsByEmail(new Email("john.doe@example.com"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с существующим email - должно бросить исключение")
    void execute_WithExistingEmail_ShouldThrowException() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "existing@example.com", 30);

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                createUserUseCase.execute(request)
        );

        verify(userRepository).existsByEmail(new Email("existing@example.com"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя без возраста")
    void execute_WithNullAge_ShouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", null);
        User savedUser = new User(1L, "John Doe", new Email("john@example.com"), null, null);

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse response = createUserUseCase.execute(request);

        // Then
        assertNotNull(response);
        assertNull(response.getAge());
    }
}
