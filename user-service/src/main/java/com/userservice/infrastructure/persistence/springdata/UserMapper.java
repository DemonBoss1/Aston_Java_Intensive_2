package com.userservice.infrastructure.persistence.springdata;

import com.userservice.domain.model.Email;
import com.userservice.domain.model.User;
import com.userservice.infrastructure.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        return new User(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                entity.getAge(),
                entity.getCreatedAt()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;

        return new UserEntity(
                domain.getId(),
                domain.getName(),
                domain.getEmail().getValue(),
                domain.getAge(),
                domain.getCreatedAt()
        );
    }
}