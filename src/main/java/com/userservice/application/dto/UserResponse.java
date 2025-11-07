package com.userservice.application.dto;

public class UserResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final Integer age;
    private final String createdAt;

    public UserResponse(Long id, String name, String email, Integer age, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }
    public String getCreatedAt() { return createdAt; }
}
