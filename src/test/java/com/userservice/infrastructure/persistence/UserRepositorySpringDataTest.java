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
@ActiveProfiles("springdata")
class UserRepositorySpringDataTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSpringDataImplementation() {
        User user = new User("SpringData Test", new Email("springdata@example.com"), 30);
        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals("springdata@example.com", saved.getEmail().getValue());

        userRepository.delete(saved.getId());
    }
}