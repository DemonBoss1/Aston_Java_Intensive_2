package com.userservice.infrastructure.persistence.springdata;

import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import com.userservice.domain.repository.UserRepository;
import com.userservice.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Profile({"springdata", "test"})
@Primary
@RequiredArgsConstructor
public class SpringDataUserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(Long id) {
        log.debug("[SPRING-DATA-JPA] Поиск пользователя по ID: {}", id);

        Optional<UserEntity> entity = jpaUserRepository.findById(id);

        if (entity.isPresent()) {
            log.debug("[SPRING-DATA-JPA] Пользователь найден: ID={}", id);
        } else {
            log.debug("[SPRING-DATA-JPA] Пользователь не найден: ID={}", id);
        }

        return entity.map(userMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        log.debug("[SPRING-DATA-JPA] Получение всех пользователей");

        List<UserEntity> entities = jpaUserRepository.findAll();
        log.debug("[SPRING-DATA-JPA] Найдено {} пользователей", entities.size());

        return entities.stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        log.debug("[SPRING-DATA-JPA] Поиск пользователя по email: {}", email.getValue());

        Optional<UserEntity> entity = jpaUserRepository.findByEmail(email.getValue());

        if (entity.isPresent()) {
            log.debug("[SPRING-DATA-JPA] Пользователь найден по email {}: ID {}", email.getValue(), entity.get().getId());
        } else {
            log.debug("[SPRING-DATA-JPA] Пользователь не найден по email: {}", email.getValue());
        }

        return entity.map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        log.info("[SPRING-DATA-JPA] Сохранение пользователя: {}", user.getEmail().getValue());

        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(entity);
        User savedUser = userMapper.toDomain(savedEntity);

        log.info("[SPRING-DATA-JPA] Пользователь успешно сохранен: {} (ID: {})",
                savedUser.getEmail().getValue(), savedUser.getId());

        return savedUser;
    }

    @Override
    public void update(User user) {
        log.info("[SPRING-DATA-JPA] Обновление пользователя: {} (ID: {})",
                user.getEmail().getValue(), user.getId());

        // Проверяем существование пользователя
        if (!jpaUserRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }

        UserEntity entity = userMapper.toEntity(user);
        jpaUserRepository.save(entity);

        log.info("[SPRING-DATA-JPA] Пользователь успешно обновлен: {} (ID: {})",
                user.getEmail().getValue(), user.getId());
    }

    @Override
    public void delete(Long id) {
        log.info("[SPRING-DATA-JPA] Удаление пользователя с ID: {}", id);

        if (!jpaUserRepository.existsById(id)) {
            log.warn("[SPRING-DATA-JPA] Пользователь для удаления не найден: ID {}", id);
            return;
        }

        jpaUserRepository.deleteById(id);
        log.info("[SPRING-DATA-JPA] Пользователь успешно удален: ID {}", id);
    }

    @Override
    public boolean existsByEmail(Email email) {
        log.debug("[SPRING-DATA-JPA] Проверка существования пользователя с email: {}", email.getValue());

        boolean exists = jpaUserRepository.existsByEmail(email.getValue());
        log.debug("[SPRING-DATA-JPA] Пользователь с email {} {}существует",
                email.getValue(), exists ? "" : "не ");

        return exists;
    }
}