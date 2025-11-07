package com.userservice.infrastructure.persistence;

import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import com.userservice.infrastructure.config.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryImplTest {

    private static UserRepositoryImpl userRepository;

    @BeforeAll
    static void setUp() {
        SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
        userRepository = new UserRepositoryImpl();

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createMutationQuery("DELETE FROM UserEntity").executeUpdate();
            transaction.commit();
        }
    }

    @AfterAll
    static void tearDown() {
        HibernateConfig.shutdown();
    }

    @Test
    @DisplayName("Сохранение и поиск пользователя по ID")
    void saveAndFindById_ShouldWorkCorrectly() {
        // Given
        User user = new User("Test User", new Email("test@example.com"), 25);

        // When
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("Test User", foundUser.get().getName());
        assertEquals("test@example.com", foundUser.get().getEmail().getValue());
        assertEquals(25, foundUser.get().getAge());
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void findByEmail_ShouldReturnUser() {
        // Given
        String email = "findbyemail@example.com";
        User user = new User("Find By Email", new Email(email), 30);
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail(new Email(email));

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail().getValue());
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void findAll_ShouldReturnAllUsers() {
        // Given
        userRepository.save(new User("User One", new Email("one@example.com"), 25));
        userRepository.save(new User("User Two", new Email("two@example.com"), 30));

        // When
        List<User> users = userRepository.findAll();

        // Then
        assertFalse(users.isEmpty());
        assertTrue(users.size() >= 2);
    }

    @Test
    @DisplayName("Обновление пользователя")
    void update_ShouldModifyUser() {
        // Given
        User user = userRepository.save(new User("Original Name", new Email("update@example.com"), 25));

        // When
        User updatedUser = user.update("Updated Name", new Email("updated@example.com"), 30);
        userRepository.update(updatedUser);

        Optional<User> foundUser = userRepository.findById(user.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("Updated Name", foundUser.get().getName());
        assertEquals("updated@example.com", foundUser.get().getEmail().getValue());
        assertEquals(30, foundUser.get().getAge());
    }

    @Test
    @DisplayName("Удаление пользователя")
    void delete_ShouldRemoveUser() {
        // Given
        User user = userRepository.save(new User("To Delete", new Email("delete@example.com"), 25));
        Long userId = user.getId();

        // When
        userRepository.delete(userId);
        Optional<User> foundUser = userRepository.findById(userId);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Проверка существования пользователя по email")
    void existsByEmail_ShouldReturnCorrectStatus() {
        // Given
        String email = "exists@example.com";
        userRepository.save(new User("Exists Test", new Email(email), 25));

        // When & Then
        assertTrue(userRepository.existsByEmail(new Email(email)));
        assertFalse(userRepository.existsByEmail(new Email("nonexistent@example.com")));
    }
}