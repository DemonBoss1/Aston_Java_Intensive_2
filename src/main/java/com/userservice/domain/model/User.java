package com.userservice.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private final Long id;
    private final String name;
    private final Email email;
    private final Integer age;
    private final LocalDateTime createdAt;

    public User(String name, Email email, Integer age) {
        this(null, name, email, age, null);
    }

    public User(Long id, String name, Email email, Integer age, LocalDateTime createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.age = age;
        this.createdAt = createdAt;

        validate();
    }

    private void validate() {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (age != null && age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Email getEmail() { return email; }
    public Integer getAge() { return age; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isAdult() {
        return age != null && age >= 18;
    }

    public User update(String name, Email email, Integer age) {
        return new User(this.id, name, email, age, this.createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s', age=%d, createdAt=%s}",
                id, name, email.getValue(), age, createdAt);
    }
}
