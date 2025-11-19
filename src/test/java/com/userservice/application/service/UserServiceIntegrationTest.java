package com.userservice.application.service;

import com.userservice.application.dto.*;
import com.userservice.config.AppConfig;
import com.userservice.domain.repository.UserRepository;
import com.userservice.infrastructure.persistence.hibernate.HibernateConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AppConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        try (var session = HibernateConfig.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            session.createMutationQuery("DELETE FROM UserEntity").executeUpdate();
            transaction.commit();
        }
    }

    @AfterAll
    void tearDown() {
        HibernateConfig.shutdownAll();
    }

    @Test
    @DisplayName("Spring Context: Проверка загрузки контекста и бинов")
    void springContext_ShouldLoadCorrectly() {
        assertNotNull(userService, "UserService должен быть загружен через Spring DI");
        assertNotNull(userRepository, "UserRepository должен быть загружен через Spring DI");
    }

    @Test
    @DisplayName("CREATE: Создание пользователя с валидными данными")
    void createUser_WithValidData_ShouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john.doe@example.com", 30);

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals(30, response.getAge());
        assertNotNull(response.getCreatedAt());

        Optional<UserResponse> foundUser = userService.getUserById(response.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(response.getId(), foundUser.get().getId());
    }

    @Test
    @DisplayName("CREATE: Создание пользователя без возраста")
    void createUser_WithNullAge_ShouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("Jane Doe", "jane.doe@example.com", null);

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertNotNull(response);
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane.doe@example.com", response.getEmail());
        assertNull(response.getAge());
    }

    @Test
    @DisplayName("CREATE: Создание пользователя с существующим email - должно бросить исключение")
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        CreateUserRequest request1 = new CreateUserRequest("First User", "duplicate@example.com", 25);
        userService.createUser(request1);

        CreateUserRequest request2 = new CreateUserRequest("Second User", "duplicate@example.com", 30);

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(request2);
        });

        assertEquals("User with this email already exists", exception.getMessage());
    }

    @Test
    @DisplayName("CREATE: Создание пользователя с невалидным email - должно бросить исключение")
    void createUser_WithInvalidEmail_ShouldThrowException() {
        // Given
        CreateUserRequest request = new CreateUserRequest("Test User", "invalid-email", 25);

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(request);
        });

        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("CREATE: Создание пользователя с пустым именем - должно бросить исключение")
    void createUser_WithEmptyName_ShouldThrowException() {
        // Given
        CreateUserRequest request = new CreateUserRequest("   ", "test@example.com", 25);

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(request);
        });

        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    @DisplayName("READ: Получение пользователя по существующему ID")
    void getUserById_WithExistingId_ShouldReturnUser() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest("Read Test", "read@example.com", 35);
        UserResponse createdUser = userService.createUser(createRequest);
        Long userId = createdUser.getId();

        // When
        Optional<UserResponse> foundUser = userService.getUserById(userId);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("Read Test", foundUser.get().getName());
        assertEquals("read@example.com", foundUser.get().getEmail());
        assertEquals(35, foundUser.get().getAge());
    }

    @Test
    @DisplayName("READ: Получение пользователя по несуществующему ID")
    void getUserById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        Long nonExistingId = 999L;

        // When
        Optional<UserResponse> foundUser = userService.getUserById(nonExistingId);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("READ: Получение пользователя по email")
    void getUserByEmail_WithExistingEmail_ShouldReturnUser() {
        // Given
        String email = "findbyemail@example.com";
        CreateUserRequest createRequest = new CreateUserRequest("Email Search", email, 28);
        userService.createUser(createRequest);

        // When
        Optional<UserResponse> foundUser = userService.getUserByEmail(email);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("Email Search", foundUser.get().getName());
        assertEquals(email, foundUser.get().getEmail());
        assertEquals(28, foundUser.get().getAge());
    }

    @Test
    @DisplayName("READ: Получение пользователя по несуществующему email")
    void getUserByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        // Given
        String nonExistingEmail = "nonexistent@example.com";

        // When
        Optional<UserResponse> foundUser = userService.getUserByEmail(nonExistingEmail);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("READ: Получение всех пользователей из пустой базы")
    void getAllUsers_WithEmptyDatabase_ShouldReturnEmptyList() {
        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("READ: Получение всех пользователей с несколькими записями")
    void getAllUsers_WithMultipleUsers_ShouldReturnAllUsers() {
        // Given
        userService.createUser(new CreateUserRequest("User One", "one@example.com", 25));
        userService.createUser(new CreateUserRequest("User Two", "two@example.com", 30));
        userService.createUser(new CreateUserRequest("User Three", "three@example.com", 35));

        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertNotNull(users);
        assertEquals(3, users.size());

        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("one@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("two@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("three@example.com")));
    }

    @Test
    @DisplayName("UPDATE: Обновление пользователя с валидными данными")
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest("Original Name", "original@example.com", 25);
        UserResponse createdUser = userService.createUser(createRequest);
        Long userId = createdUser.getId();

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                userId,
                "Updated Name",
                "updated@example.com",
                30
        );

        // When
        UserResponse updatedUser = userService.updateUser(updateRequest);

        // Then
        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());

        Optional<UserResponse> foundUser = userService.getUserById(userId);
        assertTrue(foundUser.isPresent());
        assertEquals("Updated Name", foundUser.get().getName());
        assertEquals("updated@example.com", foundUser.get().getEmail());
        assertEquals(30, foundUser.get().getAge());
    }

    @Test
    @DisplayName("UPDATE: Обновление пользователя с изменением только имени")
    void updateUser_WithNameOnly_ShouldUpdateName() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest("Original", "partial@example.com", 25);
        UserResponse createdUser = userService.createUser(createRequest);
        Long userId = createdUser.getId();

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                userId,
                "New Name Only",
                "partial@example.com",  // тот же email
                25                      // тот же возраст
        );

        // When
        UserResponse updatedUser = userService.updateUser(updateRequest);

        // Then
        assertEquals("New Name Only", updatedUser.getName());
        assertEquals("partial@example.com", updatedUser.getEmail()); // email не изменился
        assertEquals(25, updatedUser.getAge()); // возраст не изменился
    }

    @Test
    @DisplayName("UPDATE: Обновление пользователя с дублирующимся email - должно бросить исключение")
    void updateUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        userService.createUser(new CreateUserRequest("First User", "first@example.com", 25));
        UserResponse secondUser = userService.createUser(new CreateUserRequest("Second User", "second@example.com", 30));

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                secondUser.getId(),
                "Second User Updated",
                "first@example.com",  // email первого пользователя
                35
        );

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(updateRequest);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("UPDATE: Обновление несуществующего пользователя - должно бросить исключение")
    void updateUser_WithNonExistingId_ShouldThrowException() {
        // Given
        Long nonExistingId = 999L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                nonExistingId,
                "Non Existing",
                "nonexisting@example.com",
                40
        );

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(updateRequest);
        });

        assertEquals("User not found with ID: " + nonExistingId, exception.getMessage());
    }

    @Test
    @DisplayName("UPDATE: Обновление пользователя с невалидным ID - должно бросить исключение")
    void updateUser_WithInvalidId_ShouldThrowException() {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                -1L, // невалидный ID
                "Test",
                "test@example.com",
                25
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(updateRequest);
        });
    }

    @Test
    @DisplayName("DELETE: Удаление существующего пользователя")
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest("To Delete", "delete@example.com", 25);
        UserResponse createdUser = userService.createUser(createRequest);
        Long userId = createdUser.getId();

        assertTrue(userService.getUserById(userId).isPresent());

        // When
        boolean deletionResult = userService.deleteUser(userId);

        // Then
        assertTrue(deletionResult);

        assertFalse(userService.getUserById(userId).isPresent());
        assertFalse(userService.getUserByEmail("delete@example.com").isPresent());
    }

    @Test
    @DisplayName("DELETE: Удаление несуществующего пользователя")
    void deleteUser_WithNonExistingId_ShouldReturnFalse() {
        // Given
        Long nonExistingId = 999L;

        // When
        boolean deletionResult = userService.deleteUser(nonExistingId);

        // Then
        assertFalse(deletionResult);
    }

    @Test
    @DisplayName("DELETE: Удаление пользователя с невалидным ID - должно бросить исключение")
    void deleteUser_WithInvalidId_ShouldThrowException() {
        // Given
        Long invalidId = -1L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(invalidId);
        });
    }

    @Test
    @DisplayName("DELETE: Удаление пользователя и проверка целостности данных других пользователей")
    void deleteUser_ShouldNotAffectOtherUsers() {
        // Given
        UserResponse user1 = userService.createUser(new CreateUserRequest("User One", "user1@example.com", 25));
        UserResponse user2 = userService.createUser(new CreateUserRequest("User Two", "user2@example.com", 30));
        UserResponse user3 = userService.createUser(new CreateUserRequest("User Three", "user3@example.com", 35));

        // When
        boolean deletionResult = userService.deleteUser(user2.getId());

        // Then
        assertTrue(deletionResult);

        assertTrue(userService.getUserById(user1.getId()).isPresent());
        assertEquals("User One", userService.getUserById(user1.getId()).get().getName());

        assertFalse(userService.getUserById(user2.getId()).isPresent());

        assertTrue(userService.getUserById(user3.getId()).isPresent());
        assertEquals("User Three", userService.getUserById(user3.getId()).get().getName());

        List<UserResponse> allUsers = userService.getAllUsers();
        assertEquals(2, allUsers.size());
    }

    @Test
    @DisplayName("COMPLEX: Полный CRUD цикл для одного пользователя")
    void fullCrudCycle_ForSingleUser_ShouldWorkCorrectly() {
        CreateUserRequest createRequest = new CreateUserRequest("Full Cycle User", "fullcycle@example.com", 25);
        UserResponse createdUser = userService.createUser(createRequest);
        Long userId = createdUser.getId();

        assertNotNull(userId);
        assertEquals("Full Cycle User", createdUser.getName());

        Optional<UserResponse> readAfterCreate = userService.getUserById(userId);
        assertTrue(readAfterCreate.isPresent());
        assertEquals("fullcycle@example.com", readAfterCreate.get().getEmail());

        UpdateUserRequest updateRequest = new UpdateUserRequest(userId, "Updated Full Cycle", "updatedfull@example.com", 30);
        UserResponse updatedUser = userService.updateUser(updateRequest);

        assertEquals("Updated Full Cycle", updatedUser.getName());
        assertEquals("updatedfull@example.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());

        Optional<UserResponse> readAfterUpdate = userService.getUserById(userId);
        assertTrue(readAfterUpdate.isPresent());
        assertEquals("Updated Full Cycle", readAfterUpdate.get().getName());

        boolean deleteResult = userService.deleteUser(userId);
        assertTrue(deleteResult);

        Optional<UserResponse> readAfterDelete = userService.getUserById(userId);
        assertFalse(readAfterDelete.isPresent());
    }

    @Test
    @DisplayName("COMPLEX: Создание нескольких пользователей и проверка уникальности email")
    void createMultipleUsers_WithUniqueEmails_ShouldWorkCorrectly() {
        // Given
        String[] names = {"Alice", "Bob", "Charlie"};
        String[] emails = {"alice@example.com", "bob@example.com", "charlie@example.com"};
        Integer[] ages = {25, 30, 35};

        // When
        for (int i = 0; i < names.length; i++) {
            CreateUserRequest request = new CreateUserRequest(names[i], emails[i], ages[i]);
            UserResponse response = userService.createUser(request);

            assertNotNull(response.getId());
            assertEquals(names[i], response.getName());
            assertEquals(emails[i], response.getEmail());
            assertEquals(ages[i], response.getAge());
        }

        // Then
        List<UserResponse> allUsers = userService.getAllUsers();
        assertEquals(3, allUsers.size());

        long uniqueEmailCount = allUsers.stream()
                .map(UserResponse::getEmail)
                .distinct()
                .count();
        assertEquals(3, uniqueEmailCount);
    }

    @Test
    @DisplayName("COMPLEX: Поиск по email после обновления email")
    void getUserByEmail_AfterEmailUpdate_ShouldFindByNewEmail() {
        // Given
        UserResponse user = userService.createUser(
                new CreateUserRequest("Email Update Test", "old@example.com", 25)
        );

        // When
        userService.updateUser(new UpdateUserRequest(
                user.getId(), "Email Update Test", "new@example.com", 25
        ));

        // Then
        Optional<UserResponse> byNewEmail = userService.getUserByEmail("new@example.com");
        assertTrue(byNewEmail.isPresent());
        assertEquals(user.getId(), byNewEmail.get().getId());

        Optional<UserResponse> byOldEmail = userService.getUserByEmail("old@example.com");
        assertFalse(byOldEmail.isPresent());
    }
}