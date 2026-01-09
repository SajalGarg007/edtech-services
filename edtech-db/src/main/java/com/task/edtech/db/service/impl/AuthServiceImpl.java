package com.task.edtech.db.service.impl;

import com.task.edtech.db.converter.UserConverter;
import com.task.edtech.db.dto.AuthResponse;
import com.task.edtech.db.dto.LoginRequest;
import com.task.edtech.db.dto.UserDTO;
import com.task.edtech.db.dto.SignupRequest;
import com.task.edtech.db.entity.User;
import com.task.edtech.db.repository.UserRepository;
import com.task.edtech.db.security.JwtUtil;
import com.task.edtech.db.service.AuthService;
import com.task.edtech.db.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl
        implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponse signup(SignupRequest signupRequest) {

        if (userService.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(signupRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signupRequest.getPassword()))
                .name(signupRequest.getName())
                .userType(signupRequest.getUserType())
                .build();

        User dbUser = userService.addOrUpdate(user);

        String token = jwtUtil.generateToken(dbUser.getEmail(), dbUser.getId());

        UserDTO userDTO = userConverter.toDto(dbUser);

        return new AuthResponse(token, userDTO);
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        User user = userService.findByEmail(loginRequest.getEmail());

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        UserDTO userDTO = userConverter.toDto(user);

        return new AuthResponse(token, userDTO);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String email = authentication.getName();

        return userService.findByEmail(email);
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}

