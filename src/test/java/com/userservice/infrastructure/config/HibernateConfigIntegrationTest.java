package com.userservice.infrastructure.config;

import com.userservice.infrastructure.entity.UserEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
class HibernateConfigIntegrationTest {

    private SessionFactory sessionFactory;

    @BeforeAll
    void setUp() {
        sessionFactory = createNewSessionFactory();
    }

    @AfterAll
    void tearDown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createMutationQuery("DELETE FROM UserEntity").executeUpdate();
            transaction.commit();
        }
    }

    private SessionFactory createNewSessionFactory() {
        try {
            return HibernateConfig.getSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SessionFactory for test", e);
        }
    }

    @Test
    @DisplayName("Создание SessionFactory - должно быть успешным")
    void getSessionFactory_ShouldCreateSessionFactory() {
        // When & Then
        assertNotNull(sessionFactory, "SessionFactory должна быть создана");
        assertFalse(sessionFactory.isClosed(), "SessionFactory должна быть открыта");
    }

    @Test
    @DisplayName("Открытие сессии - должно работать корректно")
    void openSession_ShouldWorkCorrectly() {
        // When
        try (Session session = sessionFactory.openSession()) {
            // Then
            assertNotNull(session, "Сессия должна быть создана");
            assertTrue(session.isOpen(), "Сессия должна быть открыта");
            assertDoesNotThrow(() -> {
                session.createNativeQuery("SELECT 1", Integer.class).getSingleResult();
            });
        }
    }

    @Test
    @DisplayName("Проверка подключения к БД - выполнение простого SQL запроса")
    void databaseConnection_ShouldWork() {
        // When
        try (Session session = sessionFactory.openSession()) {
            Integer result = session.createNativeQuery("SELECT 1", Integer.class).getSingleResult();

            // Then
            assertEquals(1, result, "Запрос SELECT 1 должен вернуть 1");
        }
    }

    @Test
    @DisplayName("Создание и сохранение тестовой сущности - проверка маппинга")
    void saveEntity_ShouldWorkCorrectly() {
        // Given
        UserEntity user = new UserEntity();
        user.setName("Test User");
        user.setEmail("test.hibernate@example.com");
        user.setAge(25);

        // When
        Long userId;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            userId = user.getId();
        }

        // Then
        assertNotNull(userId, "ID должен быть сгенерирован после сохранения");
        assertTrue(userId > 0, "ID должен быть положительным числом");

        try (Session session = sessionFactory.openSession()) {
            UserEntity foundUser = session.find(UserEntity.class, userId);
            assertNotNull(foundUser, "Сохраненный пользователь должен быть найден");
            assertEquals("Test User", foundUser.getName());
            assertEquals("test.hibernate@example.com", foundUser.getEmail());
            assertEquals(25, foundUser.getAge());
        }
    }

    @Test
    @DisplayName("Транзакции - должны работать корректно")
    void transactions_ShouldWorkCorrectly() {
        // Given
        UserEntity user = new UserEntity();
        user.setName("Transaction Test");
        user.setEmail("transaction@example.com");
        user.setAge(30);

        // When & Then
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            assertTrue(transaction.isActive(), "Транзакция должна быть активна");

            session.persist(user);
            transaction.commit();

            assertFalse(transaction.isActive(), "Транзакция должна быть завершена после commit");
        }
    }

    @Test
    @DisplayName("Rollback транзакции - должен откатывать изменения")
    void transactionRollback_ShouldRevertChanges() {
        // Given
        UserEntity user = new UserEntity();
        user.setName("Rollback Test");
        user.setEmail("rollback@example.com");
        user.setAge(35);

        Long userId;

        // When
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            userId = user.getId();
            transaction.rollback();
        }

        // Then
        try (Session session = sessionFactory.openSession()) {
            UserEntity foundUser = session.find(UserEntity.class, userId);
            assertNull(foundUser, "Пользователь не должен быть найден после rollback");
        }
    }

    @Test
    @DisplayName("Проверка маппинга сущности - все поля должны сохраняться корректно")
    void entityMapping_ShouldWorkCorrectly() {
        // Given
        UserEntity user = new UserEntity();
        user.setName("Mapping Test");
        user.setEmail("mapping@example.com");
        user.setAge(40);

        // When
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        }

        // Then
        try (Session session = sessionFactory.openSession()) {
            UserEntity foundUser = session.find(UserEntity.class, user.getId());

            assertNotNull(foundUser, "Пользователь должен быть найден");
            assertEquals("Mapping Test", foundUser.getName());
            assertEquals("mapping@example.com", foundUser.getEmail());
            assertEquals(40, foundUser.getAge());
            assertNotNull(foundUser.getCreatedAt(), "created_at должен быть установлен автоматически");
        }
    }

    @Test
    @DisplayName("Обновление сущности - должно работать корректно")
    void updateEntity_ShouldWorkCorrectly() {
        // Given
        UserEntity user = new UserEntity();
        user.setName("Original Name");
        user.setEmail("update@example.com");
        user.setAge(25);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        }

        // When
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            UserEntity userToUpdate = session.find(UserEntity.class, user.getId());
            userToUpdate.setName("Updated Name");
            userToUpdate.setAge(30);
            session.merge(userToUpdate);
            transaction.commit();
        }

        // Then
        try (Session session = sessionFactory.openSession()) {
            UserEntity updatedUser = session.find(UserEntity.class, user.getId());
            assertEquals("Updated Name", updatedUser.getName());
            assertEquals(30, updatedUser.getAge());
            assertEquals("update@example.com", updatedUser.getEmail());
        }
    }

    @Test
    @DisplayName("Удаление сущности - должно работать корректно")
    void deleteEntity_ShouldWorkCorrectly() {
        // Given
        UserEntity user = new UserEntity();
        user.setName("To Delete");
        user.setEmail("delete@example.com");
        user.setAge(25);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        }

        Long userId = user.getId();
        assertNotNull(userId);

        // When
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            UserEntity userToDelete = session.find(UserEntity.class, userId);
            session.remove(userToDelete);
            transaction.commit();
        }

        // Then
        try (Session session = sessionFactory.openSession()) {
            UserEntity deletedUser = session.find(UserEntity.class, userId);
            assertNull(deletedUser, "Пользователь должен быть удален");
        }
    }
}
