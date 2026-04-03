package com.example.expensetracker.service;


import com.example.expensetracker.dto.AuthDto;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .currency("USD")
                .monthlyBudget(BigDecimal.ZERO)
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthDto.AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getCurrency(),
                user.getMonthlyBudget()
        );
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(user);
        return new AuthDto.AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getCurrency(),
                user.getMonthlyBudget()
        );
    }
}
