package com.userservice.application.usecase;

import com.userservice.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserUseCase {
    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (userRepository.findById(id).isEmpty()) {
            return false;
        }

        userRepository.delete(id);
        return true;
    }
}
