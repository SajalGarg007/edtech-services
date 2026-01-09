package com.task.edtech.db.service.impl;

import com.task.edtech.db.entity.User;
import com.task.edtech.db.exception.EntityNotFoundException;
import com.task.edtech.db.repository.UserRepository;
import com.task.edtech.db.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findById(@NotNull Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public User findByInternalId(@NotNull UUID internalId) {
        return userRepository.findByInternalId(internalId).orElseThrow(
                () -> new EntityNotFoundException("User not found with internal ID: " + internalId));
    }

    @Override
    public User findByEmail(@NotNull String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Override
    public boolean existsByEmail(@NotNull String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User addOrUpdate(@NotNull User user) {
        User dbUser = null;
        if (Objects.nonNull(user.getInternalId())) {
            dbUser = userRepository.findByInternalId(user.getInternalId())
                    .orElse(null);
        } else if (Objects.nonNull(user.getId())) {
            dbUser = userRepository.findById(user.getId())
                    .orElse(null);
        }

        if (Objects.nonNull(user.getEmail())) {
            dbUser = userRepository.findByEmail(user.getEmail())
                    .orElse(null);
        }

        if (Objects.nonNull(dbUser)) {
            dbUser.copy(user);
        } else {
            dbUser = user;
        }

        log.info("adding/updating user with id %s", user.getId());
        User savedUser = userRepository.save(dbUser);
        log.info("added/updated user with id %s", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public void delete(@NotNull User user) {
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void deleteById(@NotNull Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }
}

