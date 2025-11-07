package com.userservice.infrastructure.persistence;

import com.userservice.domain.model.User;
import com.userservice.domain.model.Email;
import com.userservice.domain.repository.UserRepository;
import com.userservice.infrastructure.config.HibernateConfig;
import com.userservice.infrastructure.entity.UserEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            UserEntity entity = session.get(UserEntity.class, id);
            return Optional.ofNullable(toDomain(entity));
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            List<UserEntity> entities = session.createQuery(
                    "FROM UserEntity",
                    UserEntity.class
            ).list();
            return entities.stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<UserEntity> query = session.createQuery(
                    "FROM UserEntity WHERE email = :email",
                    UserEntity.class
            );
            query.setParameter("email", email.getValue());
            UserEntity entity = query.uniqueResult();
            return Optional.ofNullable(toDomain(entity));
        }
    }

    @Override
    public User save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = toEntity(user);
            session.persist(entity);
            transaction.commit();
            return toDomain(entity);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = toEntity(user);
            session.merge(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity entity = session.get(UserEntity.class, id);
            if (entity != null) {
                session.remove(entity);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public boolean existsByEmail(Email email) {
        return findByEmail(email).isPresent();
    }

    private User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return new User(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                entity.getAge(),
                entity.getCreatedAt()
        );
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getName(),
                user.getEmail().getValue(),
                user.getAge(),
                user.getCreatedAt()
        );
    }
}