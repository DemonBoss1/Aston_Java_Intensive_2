package com.userservice.infrastructure.config;

import com.userservice.infrastructure.entity.UserEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HibernateConfig {
    private static final Logger logger = LogManager.getLogger(HibernateConfig.class);
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            logger.debug("Начало создания SessionFactory...");

            try {
                logger.debug("Создание StandardServiceRegistry...");
                StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                        .configure("hibernate.cfg.xml")
                        .build();

                logger.debug("Создание MetadataSources...");
                Metadata metadata = new MetadataSources(standardRegistry)
                        .addAnnotatedClass(UserEntity.class)
                        .getMetadataBuilder()
                        .build();

                logger.debug("Построение SessionFactory...");
                sessionFactory = metadata.getSessionFactoryBuilder().build();

                logger.info("Hibernate SessionFactory создана успешно");
                logger.debug("SessionFactory создана: {}", sessionFactory);

                if (logger.isDebugEnabled()) {
                    logger.debug("Статистика Hibernate: {}", sessionFactory.getStatistics());
                }

            } catch (Exception e) {
                logger.error("Ошибка при создании SessionFactory", e);
                logger.debug("Детали ошибки создания SessionFactory: {}", e.getMessage(), e);
                throw new ExceptionInInitializerError("Ошибка инициализации Hibernate: " + e.getMessage());
            }
        } else {
            logger.debug("SessionFactory уже создана, возвращается существующий экземпляр");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            try {
                logger.debug("Начало закрытия SessionFactory...");

                if (!sessionFactory.isClosed()) {
                    sessionFactory.close();
                    logger.info("Hibernate SessionFactory закрыта успешно");

                    if (logger.isDebugEnabled()) {
                        logger.debug("SessionFactory закрыта: {}", sessionFactory.isClosed());
                    }
                } else {
                    logger.debug("SessionFactory уже закрыта");
                }

            } catch (Exception e) {
                logger.error("Ошибка при закрытии SessionFactory", e);
                logger.debug("Детали ошибки закрытия SessionFactory: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("SessionFactory не была создана, закрытие не требуется");
        }
    }

    public static boolean isSessionFactoryActive() {
        boolean isActive = sessionFactory != null && !sessionFactory.isClosed();
        logger.debug("Проверка состояния SessionFactory: активна = {}", isActive);
        return isActive;
    }

    public static void logSessionFactoryStats() {
        if (sessionFactory != null && logger.isDebugEnabled()) {
            try {
                var statistics = sessionFactory.getStatistics();
                logger.debug("Статистика Hibernate SessionFactory:");
                logger.debug("  - Открыто сессий: {}", statistics.getSessionOpenCount());
                logger.debug("  - Закрыто сессий: {}", statistics.getSessionCloseCount());
                logger.debug("  - Подключения: {}", statistics.getConnectCount());
                logger.debug("  - Транзакции: {}", statistics.getTransactionCount());
            } catch (Exception e) {
                logger.debug("Не удалось получить статистику SessionFactory: {}", e.getMessage());
            }
        }
    }
}
