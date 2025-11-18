package com.userservice.infrastructure.persistence;

import com.userservice.config.AppConfig;
import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import com.userservice.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AppConfig.class)
@ActiveProfiles("hibernate")
class UserRepositoryHibernateTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testHibernateImplementation() {
        User user = new User("Hibernate Test", new Email("hibernate@example.com"), 25);
        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals("hibernate@example.com", saved.getEmail().getValue());

        userRepository.delete(saved.getId());
    }
}