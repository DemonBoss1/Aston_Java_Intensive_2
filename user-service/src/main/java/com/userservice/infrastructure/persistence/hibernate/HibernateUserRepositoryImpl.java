package com.userservice.infrastructure.persistence.hibernate;

import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import com.userservice.domain.repository.UserRepository;
import com.userservice.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Profile("hibernate")
@RequiredArgsConstructor
public class HibernateUserRepositoryImpl implements UserRepository {
    private final SessionFactory sessionFactory;

    @Override
    public Optional<User> findById(Long id) {
        log.debug("[HIBERNATE] Поиск пользователя по ID: {}", id);

        try (Session session = sessionFactory.openSession()) {
            UserEntity entity = session.get(UserEntity.class, id);
            User user = toDomain(entity);

            if (user != null) {
                log.debug("[HIBERNATE] Пользователь найден: ID={}", id);
            } else {
                log.debug("[HIBERNATE] Пользователь не найден: ID={}", id);
            }

            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("[HIBERNATE] Ошибка при поиске пользователя по ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        log.debug("[HIBERNATE] Получение всех пользователей");

        try (Session session = sessionFactory.openSession()) {
            List<UserEntity> entities = session.createQuery(
                    "FROM UserEntity",
                    UserEntity.class
            ).list();

            log.debug("[HIBERNATE] Найдено {} пользователей", entities.size());
            return entities.stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[HIBERNATE] Ошибка при получении всех пользователей: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        log.debug("[HIBERNATE] Поиск пользователя по email: {}", email.getValue());

        try (Session session = sessionFactory.openSession()) {
            Query<UserEntity> query = session.createQuery(
                    "FROM UserEntity WHERE email = :email",
                    UserEntity.class
            );
            query.setParameter("email", email.getValue());
            UserEntity entity = query.uniqueResult();

            if (entity != null) {
                log.debug("[HIBERNATE] Пользователь найден по email {}: ID {}", email.getValue(), entity.getId());
            } else {
                log.debug("[HIBERNATE] Пользователь не найден по email: {}", email.getValue());
            }

            return Optional.ofNullable(toDomain(entity));
        } catch (Exception e) {
            log.error("[HIBERNATE] Ошибка при поиске пользователя по email {}: {}", email.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public User save(User user) {
        log.info("[HIBERNATE] Сохранение пользователя: {}", user.getEmail().getValue());

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = toEntity(user);
            session.persist(entity);
            transaction.commit();

            User savedUser = toDomain(entity);
            log.info("[HIBERNATE] Пользователь успешно сохранен: {} (ID: {})",
                    savedUser.getEmail().getValue(), savedUser.getId());

            return savedUser;
        } catch (Exception e) {
            if (transaction != null) {
                log.warn("[HIBERNATE] Откат транзакции при сохранении пользователя: {}", user.getEmail().getValue());
                transaction.rollback();
            }
            log.error("[HIBERNATE] Ошибка при сохранении пользователя {}: {}",
                    user.getEmail().getValue(), e.getMessage(), e);
            throw new RuntimeException("Failed to save user: " + user.getEmail().getValue(), e);
        }
    }

    @Override
    public void update(User user) {
        log.info("[HIBERNATE] Обновление пользователя: {} (ID: {})",
                user.getEmail().getValue(), user.getId());

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = toEntity(user);
            session.merge(entity);
            transaction.commit();

            log.info("[HIBERNATE] Пользователь успешно обновлен: {} (ID: {})",
                    user.getEmail().getValue(), user.getId());
        } catch (Exception e) {
            if (transaction != null) {
                log.warn("[HIBERNATE] Откат транзакции при обновлении пользователя: {} (ID: {})",
                        user.getEmail().getValue(), user.getId());
                transaction.rollback();
            }
            log.error("[HIBERNATE] Ошибка при обновлении пользователя {} (ID: {}): {}",
                    user.getEmail().getValue(), user.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update user: " + user.getEmail().getValue(), e);
        }
    }

    @Override
    public void delete(Long id) {
        log.info("[HIBERNATE] Удаление пользователя с ID: {}", id);

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = session.get(UserEntity.class, id);

            if (entity != null) {
                log.debug("[HIBERNATE] Найден пользователь для удаления: {} (ID: {})", entity.getEmail(), id);
                session.remove(entity);
                transaction.commit();
                log.info("[HIBERNATE] Пользователь успешно удален: {} (ID: {})", entity.getEmail(), id);
            } else {
                log.warn("[HIBERNATE] Пользователь для удаления не найден: ID {}", id);
                transaction.commit(); // Все равно коммитим, т.к. удалять нечего
            }
        } catch (Exception e) {
            if (transaction != null) {
                log.warn("[HIBERNATE] Откат транзакции при удалении пользователя ID {}: {}", id, e.getMessage());
                transaction.rollback();
            }
            log.error("[HIBERNATE] Ошибка при удалении пользователя ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user with ID: " + id, e);
        }
    }

    @Override
    public boolean existsByEmail(Email email) {
        log.debug("[HIBERNATE] Проверка существования пользователя с email: {}", email.getValue());

        boolean exists = findByEmail(email).isPresent();
        log.debug("[HIBERNATE] Пользователь с email {} {}существует",
                email.getValue(), exists ? "" : "не ");

        return exists;
    }

    private User toDomain(UserEntity entity) {
        if (entity == null) return null;

        User user = new User(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                entity.getAge(),
                entity.getCreatedAt()
        );

        log.trace("[HIBERNATE] Преобразование UserEntity -> User: {} (ID: {})",
                entity.getEmail(), entity.getId());

        return user;
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity(
                user.getId(),
                user.getName(),
                user.getEmail().getValue(),
                user.getAge(),
                user.getCreatedAt()
        );

        log.trace("[HIBERNATE] Преобразование User -> UserEntity: {} (ID: {})",
                user.getEmail().getValue(), user.getId());

        return entity;
    }
}