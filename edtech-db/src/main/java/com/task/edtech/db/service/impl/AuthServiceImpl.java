package com.task.edtech.db.service.impl;

import com.task.edtech.db.dto.AuthResponse;
import com.task.edtech.db.dto.LoginRequest;
import com.task.edtech.db.dto.ProviderResponse;
import com.task.edtech.db.dto.SignupRequest;
import com.task.edtech.db.entity.Provider;
import com.task.edtech.db.repository.ProviderRepository;
import com.task.edtech.db.security.JwtUtil;
import com.task.edtech.db.service.AuthService;
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
    private ProviderRepository providerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponse signup(SignupRequest signupRequest) {

        if (providerRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Provider provider = Provider.builder()
                .email(signupRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signupRequest.getPassword()))
                .name(signupRequest.getName())
                .build();

        Provider savedProvider = providerRepository.save(provider);

        String token = jwtUtil.generateToken(savedProvider.getEmail(), savedProvider.getId());

        ProviderResponse providerResponse = new ProviderResponse(
                savedProvider.getId(),
                savedProvider.getEmail(),
                savedProvider.getName()
        );

        return new AuthResponse(token, providerResponse);
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        Optional<Provider> providerOpt = providerRepository.findByEmail(loginRequest.getEmail());

        if (providerOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        Provider provider = providerOpt.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), provider.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(provider.getEmail(), provider.getId());

        ProviderResponse providerResponse = new ProviderResponse(
                provider.getId(),
                provider.getEmail(),
                provider.getName()
        );

        return new AuthResponse(token, providerResponse);
    }

    @Override
    public Provider getCurrentProvider() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated provider found");
        }

        String email = authentication.getName();

        Optional<Provider> providerOpt = providerRepository.findByEmail(email);

        if (providerOpt.isEmpty()) {
            throw new RuntimeException("Provider not found");
        }

        return providerOpt.get();
    }

    @Override
    public Long getCurrentProviderId() {
        return getCurrentProvider().getId();
    }
}

