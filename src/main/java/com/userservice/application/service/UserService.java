package com.userservice.application.service;

import com.userservice.application.dto.CreateUserRequest;
import com.userservice.application.dto.UpdateUserRequest;
import com.userservice.application.dto.UserResponse;
import com.userservice.application.usecase.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetUserByEmailUseCase getUserByEmailUseCase;

    public UserService(
            CreateUserUseCase createUserUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            GetAllUsersUseCase getAllUsersUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            GetUserByEmailUseCase getUserByEmailUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.getUserByEmailUseCase = getUserByEmailUseCase;
    }

    public UserResponse createUser(CreateUserRequest request) {
        return createUserUseCase.execute(request);
    }

    public Optional<UserResponse> getUserById(Long id) {
        return getUserByIdUseCase.execute(id);
    }

    public List<UserResponse> getAllUsers() {
        return getAllUsersUseCase.execute();
    }

    public UserResponse updateUser(UpdateUserRequest request) {
        return updateUserUseCase.execute(request);
    }

    public boolean deleteUser(Long id) {
        return deleteUserUseCase.execute(id);
    }

    public Optional<UserResponse> getUserByEmail(String email) {
        return getUserByEmailUseCase.execute(email);
    }
}