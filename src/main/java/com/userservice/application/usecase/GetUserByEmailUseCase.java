package com.userservice.application.usecase;

import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import com.userservice.domain.repository.UserRepository;
import com.userservice.application.dto.UserResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetUserByEmailUseCase {
    private final UserRepository userRepository;

    public GetUserByEmailUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserResponse> execute(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        Optional<User> user = userRepository.findByEmail(new Email(email));
        return user.map(this::toResponse);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail().getValue(),
                user.getAge(),
                user.getCreatedAt()
        );
    }
}
