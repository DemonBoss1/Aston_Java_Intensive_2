package com.userservice.presentation.controller;

import com.userservice.application.dto.CreateUserRequest;
import com.userservice.application.dto.UpdateUserRequest;
import com.userservice.application.dto.UserResponse;
import com.userservice.infrastructure.persistence.springdata.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JpaUserRepository userRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/users";
        userRepository.deleteAll(); // Очищаем базу перед каждым тестом
    }

    @Test
    @DisplayName("Полный CRUD цикл через REST API")
    void fullCrudCycle_ShouldWorkCorrectly() {
        // CREATE
        CreateUserRequest createRequest = new CreateUserRequest("Integration Test", "integration@test.com", 28);

        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                baseUrl, createRequest, UserResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponse createdUser = createResponse.getBody();
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("Integration Test", createdUser.getName());
        assertEquals("integration@test.com", createdUser.getEmail());

        Long userId = createdUser.getId();

        // READ after CREATE
        ResponseEntity<UserResponse> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + userId, UserResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse foundUser = getResponse.getBody();
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());

        // UPDATE
        UpdateUserRequest updateRequest = new UpdateUserRequest(userId, "Updated Integration", "updated@test.com", 35);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserRequest> updateEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<UserResponse> updateResponse = restTemplate.exchange(
                baseUrl + "/" + userId, HttpMethod.PUT, updateEntity, UserResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse updatedUser = updateResponse.getBody();
        assertNotNull(updatedUser);
        assertEquals("Updated Integration", updatedUser.getName());
        assertEquals("updated@test.com", updatedUser.getEmail());
        assertEquals(35, updatedUser.getAge());

        // DELETE
        restTemplate.delete(baseUrl + "/" + userId);

        // READ after DELETE
        ResponseEntity<UserResponse> getAfterDeleteResponse = restTemplate.getForEntity(
                baseUrl + "/" + userId, UserResponse.class);

        assertThat(getAfterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Создание пользователя с дублирующимся email")
    void createUser_WithDuplicateEmail_ShouldReturnBadRequest() {
        // First user
        CreateUserRequest request1 = new CreateUserRequest("User One", "duplicate@example.com", 25);
        restTemplate.postForEntity(baseUrl, request1, UserResponse.class);

        // Second user with same email
        CreateUserRequest request2 = new CreateUserRequest("User Two", "duplicate@example.com", 30);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request2, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody())).contains("already exists");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers_ShouldReturnAllUsers() {
        // Create test users
        CreateUserRequest user1 = new CreateUserRequest("User One", "one@example.com", 25);
        CreateUserRequest user2 = new CreateUserRequest("User Two", "two@example.com", 30);

        restTemplate.postForEntity(baseUrl, user1, UserResponse.class);
        restTemplate.postForEntity(baseUrl, user2, UserResponse.class);

        // Get all users
        ResponseEntity<UserResponse[]> response = restTemplate.getForEntity(baseUrl, UserResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse[] users = response.getBody();
        assertNotNull(users);
        assertTrue(users.length >= 2);
    }

    @Test
    @DisplayName("Валидация входных данных")
    void createUser_WithInvalidData_ShouldReturnBadRequest() {
        CreateUserRequest invalidRequest = new CreateUserRequest("", "invalid-email", -5);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, invalidRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String responseBody = Objects.requireNonNull(response.getBody());
        assertThat(responseBody).contains("Validation Failed");
        assertThat(responseBody).contains("Name is required");
        assertThat(responseBody).contains("Email should be valid");
        assertThat(responseBody).contains("Age must be positive or zero");
    }
}