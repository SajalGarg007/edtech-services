package com.task.edtech.api.controller;

import com.task.edtech.db.converter.UserConverter;
import com.task.edtech.db.dto.AuthResponse;
import com.task.edtech.db.dto.LoginRequest;
import com.task.edtech.db.dto.SignupRequest;
import com.task.edtech.db.dto.UserDTO;
import com.task.edtech.db.entity.User;
import com.task.edtech.db.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserConverter userConverter;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        AuthResponse response = authService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = authService.getCurrentUser();
        UserDTO userDTO = userConverter.toDto(user);
        return ResponseEntity.ok(userDTO);
    }
}

