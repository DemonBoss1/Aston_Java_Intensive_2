package com.userservice.presentation.console;

import com.userservice.application.service.UserService;
import com.userservice.application.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApplication {
    private final UserService userService;
    private final Scanner scanner;

    public ConsoleApplication(UserService userService) {
        this.userService = userService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== User Service Console ===");

        while (true) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createUser();
                    break;
                case "2":
                    getUserById();
                    break;
                case "3":
                    getAllUsers();
                    break;
                case "4":
                    updateUser();
                    break;
                case "5":
                    deleteUser();
                    break;
                case "6":
                    getUserByEmail();
                    break;
                case "7":
                    System.out.println("Выход...");
                    return;
                default:
                    System.out.println("Неверный выбор");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== Меню ===");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Список всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("6. Найти пользователя по email");
        System.out.println("7. Выход");
        System.out.print("Выберите действие: ");
    }

    private void createUser() {
        try {
            System.out.print("Введите имя: ");
            String name = scanner.nextLine();

            System.out.print("Введите email: ");
            String email = scanner.nextLine();

            System.out.print("Введите возраст: ");
            String ageString = scanner.nextLine();
            Integer age= ageString.isEmpty() ? null : Integer.parseInt(ageString);

            CreateUserRequest request = new CreateUserRequest(name, email, age);
            UserResponse response = userService.createUser(request);

            System.out.println("Пользователь создан: ID=" + response.getId());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void getUserById() {
        try {
            System.out.print("Введите ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<UserResponse> user = userService.getUserById(id);
            if (user.isPresent()) {
                UserResponse response = user.get();
                System.out.printf(
                        "Найден пользователь: ID=%d, Name=%s, Email=%s, Age=%d%n",
                        response.getId(),
                        response.getName(),
                        response.getEmail(),
                        response.getAge()
                );
            } else {
                System.out.println("Пользователь не найден");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("Пользователи не найдены");
            } else {
                System.out.println("Список пользователей (" + users.size() + "):");
                users.forEach(user ->
                        System.out.printf(
                                "  ID: %d, Name: %s, Email: %s, Age: %d%n",
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getAge()
                        )
                );
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void updateUser() {
        try {
            System.out.print("Введите ID пользователя для обновления: ");
            Long id = Long.parseLong(scanner.nextLine());

            System.out.print("Введите новое имя: ");
            String name = scanner.nextLine();

            System.out.print("Введите новый email: ");
            String email = scanner.nextLine();

            System.out.print("Введите новый возраст: ");
            String ageString = scanner.nextLine();
            Integer age= ageString.isEmpty() ? null : Integer.parseInt(ageString);

            UpdateUserRequest request = new UpdateUserRequest(id, name, email, age);
            UserResponse response = userService.updateUser(request);

            System.out.println("Пользователь обновлен: ID=" + response.getId());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void deleteUser() {
        try {
            System.out.print("Введите ID пользователя для удаления: ");
            Long id = Long.parseLong(scanner.nextLine());

            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                System.out.println("Пользователь удален: ID=" + id);
            } else {
                System.out.println("Пользователь не найден: ID=" + id);
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void getUserByEmail() {
        try {
            System.out.print("Введите email пользователя: ");
            String email = scanner.nextLine();

            Optional<UserResponse> user = userService.getUserByEmail(email);
            if (user.isPresent()) {
                UserResponse response = user.get();
                System.out.printf("Найден пользователь: ID=%d, Name=%s, Email=%s, Age=%d%n",
                        response.getId(), response.getName(), response.getEmail(), response.getAge());
            } else {
                System.out.println("Пользователь не найден");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
