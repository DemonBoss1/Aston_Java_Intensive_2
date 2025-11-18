package com.userservice;

import com.userservice.application.service.UserService;
import com.userservice.config.AppConfig;
import com.userservice.infrastructure.config.HibernateConfig;
import com.userservice.infrastructure.persistence.UserRepositoryImpl;
import com.userservice.presentation.console.ConsoleApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("Запуск User Service Application с чистой архитектурой");

        try(var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            ConsoleApplication consoleApp = context.getBean(ConsoleApplication.class);
            consoleApp.start();

        } catch (Exception e) {
            logger.severe("Критическая ошибка в приложении: " + e.getMessage());
        } finally {
            HibernateConfig.shutdown();
            logger.info("Приложение завершено");
        }
    }
}
