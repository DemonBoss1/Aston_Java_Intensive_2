package com.userservice.infrastructure.config;

import com.userservice.infrastructure.entity.UserEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateConfig {
    private static final Logger logger = LogManager.getLogger(HibernateConfig.class);
    private static final Map<String, SessionFactory> sessionFactoryCache = new ConcurrentHashMap<>();

    public static SessionFactory getSessionFactory() {
        return getSessionFactory("hibernate.cfg.xml");
    }

    public static SessionFactory getSessionFactory(String configFile) {
        return sessionFactoryCache.computeIfAbsent(configFile, key -> {
            logger.debug("Создание SessionFactory для конфигурации: {}", configFile);

            try {
                StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                        .configure(configFile)
                        .build();

                Metadata metadata = new MetadataSources(standardRegistry)
                        .addAnnotatedClass(UserEntity.class)
                        .getMetadataBuilder()
                        .build();

                SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();

                logger.info("✅ Hibernate SessionFactory создана успешно для {}", configFile);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (sessionFactory != null && !sessionFactory.isClosed()) {
                        sessionFactory.close();
                        logger.info("SessionFactory закрыта через shutdown hook");
                    }
                }));

                return sessionFactory;

            } catch (Exception e) {
                logger.error("❌ Ошибка при создании SessionFactory для {}", configFile, e);
                throw new ExceptionInInitializerError("Ошибка инициализации Hibernate: " + e.getMessage());
            }
        });
    }

    public static SessionFactory createTestSessionFactory(String jdbcUrl, String username, String password) {
        logger.debug("Создание тестовой SessionFactory для: {}", jdbcUrl);

        try {
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.connection.driver_class", "org.postgresql.Driver")
                    .applySetting("hibernate.connection.url", jdbcUrl)
                    .applySetting("hibernate.connection.username", username)
                    .applySetting("hibernate.connection.password", password)
                    .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                    .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                    .applySetting("hibernate.show_sql", "true")
                    .applySetting("hibernate.format_sql", "true")
                    .applySetting("hibernate.connection.pool_size", "5")
                    .build();

            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(UserEntity.class)
                    .getMetadataBuilder()
                    .build();

            SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();

            logger.info("✅ Тестовая SessionFactory создана успешно");
            return sessionFactory;

        } catch (Exception e) {
            logger.error("❌ Ошибка при создании тестовой SessionFactory", e);
            throw new ExceptionInInitializerError("Ошибка инициализации тестовой Hibernate: " + e.getMessage());
        }
    }

    public static void shutdown() {
        shutdown("hibernate.cfg.xml");
    }

    public static void shutdown(String configFile) {
        SessionFactory sessionFactory = sessionFactoryCache.get(configFile);
        if (sessionFactory != null) {
            try {
                logger.debug("Закрытие SessionFactory для: {}", configFile);

                if (!sessionFactory.isClosed()) {
                    sessionFactory.close();
                    sessionFactoryCache.remove(configFile);
                    logger.info("✅ SessionFactory закрыта успешно для {}", configFile);
                }

            } catch (Exception e) {
                logger.error("❌ Ошибка при закрытии SessionFactory для {}", configFile, e);
            }
        }
    }

    public static void shutdownAll() {
        logger.debug("Закрытие всех SessionFactory");
        sessionFactoryCache.forEach((key, sessionFactory) -> {
            try {
                if (!sessionFactory.isClosed()) {
                    sessionFactory.close();
                }
            } catch (Exception e) {
                logger.error("Ошибка при закрытии SessionFactory для {}", key, e);
            }
        });
        sessionFactoryCache.clear();
        logger.info("✅ Все SessionFactory закрыты");
    }

    public static boolean isSessionFactoryActive(String configFile) {
        SessionFactory sessionFactory = sessionFactoryCache.get(configFile);
        return sessionFactory != null && !sessionFactory.isClosed();
    }
}