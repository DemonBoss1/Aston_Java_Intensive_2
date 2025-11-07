package com.userservice.infrastructure.config;

import com.userservice.infrastructure.entity.UserEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.logging.Logger;

public class HibernateConfig {
    private static final Logger logger = Logger.getLogger(HibernateConfig.class.getName());
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                        .configure("hibernate.cfg.xml")
                        .build();

                Metadata metadata = new MetadataSources(standardRegistry)
                        .addAnnotatedClass(UserEntity.class)
                        .getMetadataBuilder()
                        .build();

                sessionFactory = metadata.getSessionFactoryBuilder().build();
                logger.info("Hibernate SessionFactory создана успешно");

            } catch (Exception e) {
                logger.severe("Ошибка при создании SessionFactory: " + e.getMessage());
                throw new ExceptionInInitializerError("Ошибка инициализации Hibernate: " + e.getMessage());
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            logger.info("Hibernate SessionFactory закрыта");
        }
    }
}