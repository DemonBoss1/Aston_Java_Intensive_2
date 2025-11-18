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
        User existingUser = findUserById(request.getId());
        Email newEmail = new Email(request.getEmail());

        ensureEmailIsAvailable(existingUser, newEmail);

        User updatedUser = existingUser.update(
                request.getName(),
                newEmail,
                request.getAge());

        userRepository.update(updatedUser);

        return toResponse(updatedUser);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    private void ensureEmailIsAvailable(User existingUser, Email newEmail) {
        boolean isEmailChanged = !existingUser.getEmail().equals(newEmail);
        boolean isEmailTaken = userRepository.existsByEmail(newEmail);

        if (isEmailChanged && isEmailTaken) {
            throw new IllegalArgumentException("User with email " + newEmail.getValue() + " already exists");
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail().getValue(),
                user.getAge(),
                user.getCreatedAt().toString()
        );
    }
}
