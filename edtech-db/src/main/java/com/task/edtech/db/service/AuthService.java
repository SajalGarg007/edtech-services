package com.task.edtech.db.service;

import com.task.edtech.db.dto.AuthResponse;
import com.task.edtech.db.dto.LoginRequest;
import com.task.edtech.db.dto.SignupRequest;
import com.task.edtech.db.entity.User;

public interface AuthService {

    AuthResponse signup(SignupRequest signupRequest);

    AuthResponse login(LoginRequest loginRequest);

    User getCurrentUser();

    Long getCurrentUserId();
}
