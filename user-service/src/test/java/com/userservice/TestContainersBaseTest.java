package com.userservice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class TestContainersBaseTest {

    protected static PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("test_user_service")
                .withUsername("test")
                .withPassword("test");
    }

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
        System.setProperty("DB_URL", postgresContainer.getJdbcUrl());
        System.setProperty("DB_USERNAME", postgresContainer.getUsername());
        System.setProperty("DB_PASSWORD", postgresContainer.getPassword());
    }

    @AfterAll
    static void afterAll() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }

    protected static String getJdbcUrl() {
        return postgresContainer.getJdbcUrl();
    }

    protected static String getUsername() {
        return postgresContainer.getUsername();
    }

    protected static String getPassword() {
        return postgresContainer.getPassword();
    }
}
