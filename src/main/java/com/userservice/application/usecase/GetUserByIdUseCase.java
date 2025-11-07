package com.userservice.application.usecase;

import com.userservice.domain.model.User;
import com.userservice.domain.repository.UserRepository;
import com.userservice.application.dto.UserResponse;

import java.util.Optional;

public class GetUserByIdUseCase {
    private final UserRepository userRepository;

    public GetUserByIdUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserResponse> execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Optional<User> user = userRepository.findById(id);
        return user.map(this::toResponse);
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
