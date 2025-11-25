package com.userservice.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    @DisplayName("Создание email с валидным форматом")
    void createEmail_WithValidFormat_ShouldCreateEmail() {
        // Given
        String validEmail = "test@example.com";

        // When
        Email email = new Email(validEmail);

        // Then
        assertEquals(validEmail, email.getValue());
    }

    @Test
    @DisplayName("Создание email с null - должно бросить исключение")
    void createEmail_WithNull_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email",
            "invalid@",
            "@example.com",
            "invalid@example",
            "invalid@.com"
    })
    @DisplayName("Создание email с невалидным форматом - должно бросить исключение")
    void createEmail_WithInvalidFormat_ShouldThrowException(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "valid@example.com",
            "valid.name@example.com",
            "valid_name@example.com",
            "valid+tag@example.com",
            "valid@sub.example.com"
    })
    @DisplayName("Создание email с различными валидными форматами")
    void createEmail_WithVariousValidFormats_ShouldCreateEmail(String validEmail) {
        assertDoesNotThrow(() -> {
            Email email = new Email(validEmail);
            assertEquals(validEmail, email.getValue());
        });
    }

    @Test
    @DisplayName("Проверка equals для одинаковых email")
    void equals_WithSameValue_ShouldReturnTrue() {
        // Given
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("test@example.com");

        // When & Then
        assertEquals(email1, email2);
    }

    @Test
    @DisplayName("Проверка equals для разных email")
    void equals_WithDifferentValue_ShouldReturnFalse() {
        // Given
        Email email1 = new Email("test1@example.com");
        Email email2 = new Email("test2@example.com");

        // When & Then
        assertNotEquals(email1, email2);
    }
}
