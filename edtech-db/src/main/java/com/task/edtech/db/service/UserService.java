package com.task.edtech.db.service;

import com.task.edtech.db.entity.User;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface UserService {

    User findById(@NotNull Long userId);

    User findByInternalId(@NotNull UUID internalId);

    User findByEmail(@NotNull String email);

    boolean existsByEmail(@NotNull String email);

    User addOrUpdate(@NotNull User user);

    void delete(@NotNull User user);

    void deleteById(@NotNull Long userId);
}

