package com.task.edtech.db.dto;

import com.task.edtech.db.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private UUID internalId;
    private String email;
    private String name;
    private UserType userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

