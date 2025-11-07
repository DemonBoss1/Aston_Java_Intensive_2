package com.userservice.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    void createUser_WithValidData_ShouldCreateUser() {
        // Given
        String name = "John Doe";
        Email email = new Email("john.doe@example.com");
        Integer age = 30;

        // When
        User user = new User(name, email, age);

        // Then
        assertNull(user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(age, user.getAge());
        assertNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("Создание пользователя с null именем - должно бросить исключение")
    void createUser_WithNullName_ShouldThrowException() {
        // Given
        Email email = new Email("test@example.com");

        // When & Then
        assertThrows(NullPointerException.class, () ->
                new User(null, email, 25)
        );
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем - должно бросить исключение")
    void createUser_WithEmptyName_ShouldThrowException() {
        // Given
        Email email = new Email("test@example.com");

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new User("   ", email, 25)
        );
    }

    @Test
    @DisplayName("Создание пользователя с отрицательным возрастом - должно бросить исключение")
    void createUser_WithNegativeAge_ShouldThrowException() {
        // Given
        Email email = new Email("test@example.com");

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new User("John Doe", email, -5)
        );
    }

    @Test
    @DisplayName("Проверка isAdult для совершеннолетнего пользователя")
    void isAdult_WithAge18OrMore_ShouldReturnTrue() {
        // Given
        User user = new User("John Doe", new Email("john@example.com"), 18);

        // When & Then
        assertTrue(user.isAdult());
    }

    @Test
    @DisplayName("Проверка isAdult для несовершеннолетнего пользователя")
    void isAdult_WithAgeLessThan18_ShouldReturnFalse() {
        // Given
        User user = new User("Jane Doe", new Email("jane@example.com"), 17);

        // When & Then
        assertFalse(user.isAdult());
    }

    @Test
    @DisplayName("Проверка isAdult для пользователя без возраста")
    void isAdult_WithNullAge_ShouldReturnFalse() {
        // Given
        User user = new User("John Doe", new Email("john@example.com"), null);

        // When & Then
        assertFalse(user.isAdult());
    }

    @Test
    @DisplayName("Обновление пользователя")
    void update_WithNewData_ShouldReturnNewUser() {
        // Given
        User originalUser = new User(1L, "Old Name", new Email("old@example.com"), 25, null);
        String newName = "New Name";
        Email newEmail = new Email("new@example.com");
        Integer newAge = 30;

        // When
        User updatedUser = originalUser.update(newName, newEmail, newAge);

        // Then
        assertEquals(originalUser.getId(), updatedUser.getId());
        assertEquals(newName, updatedUser.getName());
        assertEquals(newEmail, updatedUser.getEmail());
        assertEquals(newAge, updatedUser.getAge());
        assertEquals(originalUser.getCreatedAt(), updatedUser.getCreatedAt());
    }

    @Test
    @DisplayName("Проверка equals по email")
    void equals_WithSameEmail_ShouldReturnTrue() {
        // Given
        Email email = new Email("same@example.com");
        User user1 = new User("User One", email, 25);
        User user2 = new User("User Two", email, 30);

        // When & Then
        assertEquals(user1, user2);
    }

    @Test
    @DisplayName("Проверка hashCode согласованности")
    void hashCode_WithSameEmail_ShouldBeEqual() {
        // Given
        Email email = new Email("test@example.com");
        User user1 = new User("User One", email, 25);
        User user2 = new User("User Two", email, 30);

        // When & Then
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
