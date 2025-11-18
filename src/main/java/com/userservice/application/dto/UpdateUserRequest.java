package com.userservice.application.dto;

public class UpdateUserRequest {
    private final Long id;
    private final String name;
    private final String email;
    private final Integer age;

    public UpdateUserRequest(Long id, String name, String email, Integer age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Геттеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }
}
