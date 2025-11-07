package com.userservice;

import com.userservice.application.service.UserService;
import com.userservice.infrastructure.config.HibernateConfig;
import com.userservice.infrastructure.persistence.UserRepositoryImpl;
import com.userservice.presentation.console.ConsoleApplication;

import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("Запуск User Service Application с чистой архитектурой");

        try {
            UserRepositoryImpl userRepository = new UserRepositoryImpl();
            UserService userService = new UserService(userRepository);

            ConsoleApplication consoleApp = new ConsoleApplication(userService);
            consoleApp.start();

        } catch (Exception e) {
            logger.severe("Критическая ошибка в приложении: " + e.getMessage());
        } finally {
            HibernateConfig.shutdown();
            logger.info("Приложение завершено");
        }
    }
}