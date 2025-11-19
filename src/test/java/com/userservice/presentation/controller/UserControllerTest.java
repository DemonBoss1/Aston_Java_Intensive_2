package com.userservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.application.dto.CreateUserRequest;
import com.userservice.application.dto.UpdateUserRequest;
import com.userservice.application.dto.UserResponse;
import com.userservice.application.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
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
                .andExpect(jsonPath("$.age").value(30));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("", "invalid-email", -1);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
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
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() throws Exception {
        // Given
        UserResponse response = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.getUserById(1L)).thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(userService).getUserById(999L);
    }

    @Test
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
    void deleteUser_WithExistingId_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void getUserByEmail_WithExistingEmail_ShouldReturnUser() throws Exception {
        // Given
        UserResponse response = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.getUserByEmail("john@example.com")).thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/users/email/john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).getUserByEmail("john@example.com");
    }
}