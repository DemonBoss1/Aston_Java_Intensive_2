package com.userservice.infrastructure.persistence;

import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import com.userservice.domain.repository.UserRepository;
import com.userservice.infrastructure.config.HibernateConfig;
import com.userservice.infrastructure.entity.UserEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserRepositoryImpl implements UserRepository {
    private static final Logger logger = LogManager.getLogger(UserRepositoryImpl.class);

    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Поиск пользователя по ID: {}", id);

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            UserEntity entity = session.get(UserEntity.class, id);

            if (entity != null) {
                logger.debug("Пользователь найден по ID {}: {}", id, entity.getEmail());
            } else {
                logger.debug("Пользователь не найден по ID: {}", id);
            }

            return Optional.ofNullable(toDomain(entity));
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        logger.debug("Получение всех пользователей");

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            List<UserEntity> entities = session.createQuery(
                    "FROM UserEntity",
                    UserEntity.class
            ).list();

            logger.debug("Найдено {} пользователей", entities.size());
            return entities.stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Ошибка при получении всех пользователей: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        logger.debug("Поиск пользователя по email: {}", email.getValue());

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<UserEntity> query = session.createQuery(
                    "FROM UserEntity WHERE email = :email",
                    UserEntity.class
            );
            query.setParameter("email", email.getValue());
            UserEntity entity = query.uniqueResult();

            if (entity != null) {
                logger.debug("Пользователь найден по email {}: ID {}", email.getValue(), entity.getId());
            } else {
                logger.debug("Пользователь не найден по email: {}", email.getValue());
            }

            return Optional.ofNullable(toDomain(entity));
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по email {}: {}", email.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public User save(User user) {
        logger.info("Сохранение пользователя: {}", user.getEmail().getValue());

        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = toEntity(user);
            session.persist(entity);
            transaction.commit();

            User savedUser = toDomain(entity);
            logger.info("Пользователь успешно сохранен: {} (ID: {})",
                    savedUser.getEmail().getValue(), savedUser.getId());

            return savedUser;
        } catch (Exception e) {
            if (transaction != null) {
                logger.warn("Откат транзакции при сохранении пользователя: {}", user.getEmail().getValue());
                transaction.rollback();
            }
            logger.error("Ошибка при сохранении пользователя {}: {}",
                    user.getEmail().getValue(), e.getMessage(), e);
            throw new RuntimeException("Failed to save user: " + user.getEmail().getValue(), e);
        }
    }

    @Override
    public void update(User user) {
        logger.info("Обновление пользователя: {} (ID: {})",
                user.getEmail().getValue(), user.getId());

        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = toEntity(user);
            session.merge(entity);
            transaction.commit();

            logger.info("Пользователь успешно обновлен: {} (ID: {})",
                    user.getEmail().getValue(), user.getId());
        } catch (Exception e) {
            if (transaction != null) {
                logger.warn("Откат транзакции при обновлении пользователя: {} (ID: {})",
                        user.getEmail().getValue(), user.getId());
                transaction.rollback();
            }
            logger.error("Ошибка при обновлении пользователя {} (ID: {}): {}",
                    user.getEmail().getValue(), user.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update user: " + user.getEmail().getValue(), e);
        }
    }

    @Override
    public void delete(Long id) {
        logger.info("Удаление пользователя с ID: {}", id);

        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = session.get(UserEntity.class, id);

            if (entity != null) {
                logger.debug("Найден пользователь для удаления: {} (ID: {})", entity.getEmail(), id);
                session.remove(entity);
                transaction.commit();
                logger.info("Пользователь успешно удален: {} (ID: {})", entity.getEmail(), id);
            } else {
                logger.warn("Пользователь для удаления не найден: ID {}", id);
                transaction.commit(); // Все равно коммитим, т.к. удалять нечего
            }
        } catch (Exception e) {
            if (transaction != null) {
                logger.warn("Откат транзакции при удалении пользователя ID {}: {}", id, e.getMessage());
                transaction.rollback();
            }
            logger.error("Ошибка при удалении пользователя ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user with ID: " + id, e);
        }
    }

    @Override
    public boolean existsByEmail(Email email) {
        logger.debug("Проверка существования пользователя с email: {}", email.getValue());

        boolean exists = findByEmail(email).isPresent();
        logger.debug("Пользователь с email {} {}существует",
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

        logger.trace("Преобразование UserEntity -> User: {} (ID: {})",
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

        logger.trace("Преобразование User -> UserEntity: {} (ID: {})",
                user.getEmail().getValue(), user.getId());

        return entity;
    }
}