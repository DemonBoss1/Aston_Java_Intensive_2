package com.userservice.application.usecase;

import com.userservice.domain.model.User;
import com.userservice.domain.repository.UserRepository;
import com.userservice.application.dto.UserResponse;

import java.util.List;
import java.util.stream.Collectors;

public class GetAllUsersUseCase {
    private final UserRepository userRepository;

    public GetAllUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> execute() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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