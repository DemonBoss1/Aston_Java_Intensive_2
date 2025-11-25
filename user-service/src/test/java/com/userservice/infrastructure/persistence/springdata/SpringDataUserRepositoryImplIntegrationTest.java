package com.userservice.infrastructure.persistence.springdata;

import com.userservice.TestContainersBaseTest;
import com.userservice.config.AppConfig;
import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Отключаем замену на встроенную БД
@Import(AppConfig.class)
class SpringDataUserRepositoryImplIntegrationTest extends TestContainersBaseTest {

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private SpringDataUserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        jpaUserRepository.deleteAll();
    }

    @Test
    @DisplayName("Сохранение и поиск пользователя по ID")
    void saveAndFindById_ShouldWorkCorrectly() {
        // Given
        User user = new User("John Doe", new Email("john.doe@example.com"), 30);

        // When
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
        assertThat(foundUser.get().getEmail().getValue()).isEqualTo("john.doe@example.com");
        assertThat(foundUser.get().getAge()).isEqualTo(30);
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
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail().getValue()).isEqualTo(email);
        assertThat(foundUser.get().getName()).isEqualTo("Find By Email");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void findAll_ShouldReturnAllUsers() {
        // Given
        userRepository.save(new User("User One", new Email("one@example.com"), 25));
        userRepository.save(new User("User Two", new Email("two@example.com"), 30));
        userRepository.save(new User("User Three", new Email("three@example.com"), 35));

        // When
        List<User> users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("User One", "User Two", "User Three");
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
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Updated Name");
        assertThat(foundUser.get().getEmail().getValue()).isEqualTo("updated@example.com");
        assertThat(foundUser.get().getAge()).isEqualTo(30);
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
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования пользователя по email")
    void existsByEmail_ShouldReturnCorrectStatus() {
        // Given
        String email = "exists@example.com";
        userRepository.save(new User("Exists Test", new Email(email), 25));

        // When & Then
        assertThat(userRepository.existsByEmail(new Email(email))).isTrue();
        assertThat(userRepository.existsByEmail(new Email("nonexistent@example.com"))).isFalse();
    }

    @Test
    @DisplayName("Поиск несуществующего пользователя по ID")
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Сохранение пользователя без возраста")
    void save_WithNullAge_ShouldWorkCorrectly() {
        // Given
        User user = new User("No Age User", new Email("noage@example.com"), null);

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getAge()).isNull();
        assertThat(userRepository.findById(savedUser.getId())).isPresent()
                .get()
                .extracting(User::getAge)
                .isNull();
    }
}