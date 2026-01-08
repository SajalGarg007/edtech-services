package com.task.edtech.db.service;

import com.task.edtech.db.dto.AuthResponse;
import com.task.edtech.db.dto.LoginRequest;
import com.task.edtech.db.dto.SignupRequest;
import com.task.edtech.db.entity.User;

public interface AuthService {

    /**
     * Sign up a new provider
     * @param signupRequest signup request with email, password, and name
     * @return AuthResponse with JWT token and provider info
     * @throws RuntimeException if email already exists
     */
    AuthResponse signup(SignupRequest signupRequest);

    /**
     * Login an existing provider
     * @param loginRequest login request with email and password
     * @return AuthResponse with JWT token and provider info
     * @throws RuntimeException if credentials are invalid
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * Get current authenticated provider from SecurityContext
     * @return Provider entity
     * @throws RuntimeException if no provider is authenticated
     */
    User getCurrentProvider();

    /**
     * Get current provider ID from SecurityContext
     * @return Provider ID
     * @throws RuntimeException if no provider is authenticated
     */
    Long getCurrentProviderId();
}
