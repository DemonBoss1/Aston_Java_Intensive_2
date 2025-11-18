package com.userservice.config;

import com.userservice.domain.repository.UserRepository;
import com.userservice.infrastructure.persistence.UserRepositoryImpl;
import com.userservice.application.service.UserService;
import com.userservice.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan("com.userservice")
public class AppConfig {

}