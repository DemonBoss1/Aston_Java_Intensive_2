package com.userservice.application.usecase;

import com.userservice.application.dto.CreateUserRequest;
import com.userservice.application.dto.UserResponse;
import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import com.userservice.domain.repository.UserRepository;

public class CreateUserUseCase {
    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse execute(CreateUserRequest request) {
        if (userRepository.existsByEmail(new Email(request.getEmail()))) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User(
                request.getName(),
                new Email(request.getEmail()),
                request.getAge()
        );

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail().getValue(),
                user.getAge(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}

