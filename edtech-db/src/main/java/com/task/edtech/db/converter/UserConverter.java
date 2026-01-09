package com.task.edtech.db.converter;

import com.task.edtech.db.dto.UserDTO;
import com.task.edtech.db.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConverter {

    public UserDTO toDto(User entity) {
        if (entity == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setInternalId(entity.getInternalId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setUserType(entity.getUserType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) return null;

        User entity = new User();
        entity.setId(dto.getId());
        entity.setInternalId(dto.getInternalId());
        entity.setEmail(dto.getEmail());
        entity.setName(dto.getName());
        entity.setUserType(dto.getUserType());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }
}

