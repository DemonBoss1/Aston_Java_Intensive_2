package com.userservice.domain.repository;

import com.userservice.domain.model.User;
import com.userservice.domain.model.Email;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    List<User> findAll();
    Optional<User> findByEmail(Email email);
    User save(User user);
    void update(User user);
    void delete(Long id);
    boolean existsByEmail(Email email);
}