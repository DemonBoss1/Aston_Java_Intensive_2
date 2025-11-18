package com.userservice.application.usecase;

import com.userservice.domain.model.User;
import com.userservice.domain.model.Email;
import com.userservice.domain.repository.UserRepository;
import com.userservice.application.dto.UpdateUserRequest;
import com.userservice.application.dto.UserResponse;

public class UpdateUserUseCase {
    private final UserRepository userRepository;

    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse execute(UpdateUserRequest request) {
        validateRequest(request);

        User existingUser = userRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getId()));

        Email newEmail = new Email(request.getEmail());
        if (!existingUser.getEmail().equals(newEmail)) {
            ensureEmailIsAvailable(newEmail);
        }

        User updatedUser = existingUser.update(
                request.getName(),
                newEmail,
                request.getAge()
        );

        userRepository.update(updatedUser);

        return toResponse(updatedUser);
    }

    private void validateRequest(UpdateUserRequest request) {
        if (request.getId() == null || request.getId() <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }

    private void ensureEmailIsAvailable(Email email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
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
