package com.userservice.application.service;

import com.userservice.application.usecase.*;
import com.userservice.application.dto.*;
import com.userservice.domain.repository.UserRepository;

import java.util.List;
import java.util.Optional;
public class UserService {
    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetUserByEmailUseCase getUserByEmailUseCase;

    public UserService(UserRepository userRepository) {
        this.createUserUseCase = new CreateUserUseCase(userRepository);
        this.getUserByIdUseCase = new GetUserByIdUseCase(userRepository);
        this.getAllUsersUseCase = new GetAllUsersUseCase(userRepository);
        this.updateUserUseCase = new UpdateUserUseCase(userRepository);
        this.deleteUserUseCase = new DeleteUserUseCase(userRepository);
        this.getUserByEmailUseCase = new GetUserByEmailUseCase(userRepository);
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