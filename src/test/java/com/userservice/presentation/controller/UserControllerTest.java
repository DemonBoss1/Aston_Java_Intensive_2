package com.userservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.application.dto.*;
import com.userservice.application.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // для работы с Java 8 time API
    }

    @Test
    @DisplayName("POST /api/v1/users - создание пользователя с валидными данными")
    void createUser_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", 30);
        UserResponse response = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users - валидация данных (пустое имя)")
    void createUser_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("", "test@example.com", 25);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("POST /api/v1/users - валидация email")
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "invalid-email", 25);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("POST /api/v1/users - отрицательный возраст")
    void createUser_WithNegativeAge_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "test@example.com", -5);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("GET /api/v1/users - получение всех пользователей")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        // Given
        List<UserResponse> users = Arrays.asList(
                new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now()),
                new UserResponse(2L, "Jane Smith", "jane@example.com", 25, LocalDateTime.now())
        );

        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - получение пользователя по существующему ID")
    void getUserById_WithExistingId_ShouldReturnUser() throws Exception {
        // Given
        UserResponse response = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.getUserById(1L)).thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - пользователь не найден")
    void getUserById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));

        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("GET /api/v1/users/email/{email} - получение пользователя по email")
    void getUserByEmail_WithExistingEmail_ShouldReturnUser() throws Exception {
        // Given
        UserResponse response = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.getUserByEmail("john@example.com")).thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/users/email/john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userService).getUserByEmail("john@example.com");
    }

    @Test
    @DisplayName("GET /api/v1/users/email/{email} - пользователь по email не найден")
    void getUserByEmail_WithNonExistingEmail_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/users/email/nonexistent@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(userService).getUserByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - обновление пользователя")
    void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(1L, "John Updated", "john.updated@example.com", 35);
        UserResponse response = new UserResponse(1L, "John Updated", "john.updated@example.com", 35, LocalDateTime.now());

        when(userService.updateUser(any(UpdateUserRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"))
                .andExpect(jsonPath("$.age").value(35));

        verify(userService).updateUser(any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - несоответствие ID в пути и теле")
    void updateUser_WithIdMismatch_ShouldReturnBadRequest() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(2L, "John Updated", "john@example.com", 35);

        // When & Then
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(userService, never()).updateUser(any());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - удаление существующего пользователя")
    void deleteUser_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Given
        when(userService.deleteUser(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - удаление несуществующего пользователя")
    void deleteUser_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.deleteUser(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(userService).deleteUser(999L);
    }

    @Test
    @DisplayName("Business Logic Error - дублирование email")
    void createUser_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "duplicate@example.com", 30);

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new IllegalArgumentException("User with this email already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User with this email already exists"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }
}