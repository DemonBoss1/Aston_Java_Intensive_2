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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetAllUsersUseCase getAllUsersUseCase;

    @Test
    @DisplayName("Получение всех пользователей - несколько записей")
    void execute_WithMultipleUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(
                new User(1L, "John Doe", new Email("john@example.com"), 30, null),
                new User(2L, "Jane Smith", new Email("jane@example.com"), 25, null),
                new User(3L, "Bob Johnson", new Email("bob@example.com"), 35, null)
        );

        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        UserResponse firstUser = result.get(0);
        assertEquals(1L, firstUser.getId());
        assertEquals("John Doe", firstUser.getName());
        assertEquals("john@example.com", firstUser.getEmail());
        assertEquals(30, firstUser.getAge());

        UserResponse secondUser = result.get(1);
        assertEquals(2L, secondUser.getId());
        assertEquals("Jane Smith", secondUser.getName());
        assertEquals("jane@example.com", secondUser.getEmail());
        assertEquals(25, secondUser.getAge());

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Получение всех пользователей - пустой список")
    void execute_WithNoUsers_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Получение всех пользователей - один пользователь")
    void execute_WithSingleUser_ShouldReturnSingleUser() {
        // Given
        User user = new User(1L, "Single User", new Email("single@example.com"), 40, null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        // When
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Then
        assertEquals(1, result.size());
        UserResponse response = result.get(0);
        assertEquals("Single User", response.getName());
        assertEquals("single@example.com", response.getEmail());
        assertEquals(40, response.getAge());

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Получение всех пользователей - пользователь без возраста")
    void execute_WithUserWithoutAge_ShouldHandleNullAge() {
        // Given
        User user = new User(1L, "No Age User", new Email("noage@example.com"), null, null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        // When
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Then
        assertEquals(1, result.size());
        assertNull(result.get(0).getAge());
        verify(userRepository).findAll();
    }
}
