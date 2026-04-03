package com.example.expensetracker.controller;


import com.example.expensetracker.dto.AuthDto;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponse> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            User currentUser = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Map<String, Object> response = new HashMap<>();
            response.put("name", currentUser.getName());
            response.put("email", currentUser.getEmail());
            response.put("currency", currentUser.getCurrency() != null ? currentUser.getCurrency() : "USD");
            response.put("monthlyBudget", currentUser.getMonthlyBudget() != null ? currentUser.getMonthlyBudget() : BigDecimal.ZERO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to load profile"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name cannot be empty"));
            }

            User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            currentUser.setName(name.trim());
            userRepository.save(currentUser);

            return ResponseEntity.ok(Map.of(
                    "name", currentUser.getName(),
                    "email", currentUser.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update profile"));
        }
    }

    @PutMapping("/budget")
    public ResponseEntity<?> updateBudget(@RequestBody Map<String, BigDecimal> request, Authentication authentication) {
        try {
            BigDecimal monthlyBudget = request.get("monthlyBudget");
            if (monthlyBudget == null || monthlyBudget.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid budget amount"));
            }

            User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            currentUser.setMonthlyBudget(monthlyBudget);
            userRepository.save(currentUser);

            return ResponseEntity.ok(Map.of("monthlyBudget", currentUser.getMonthlyBudget()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update budget"));
        }
    }

    @PutMapping("/currency")
    public ResponseEntity<?> updateCurrency(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String currency = request.get("currency");
            if (currency == null || currency.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Currency cannot be empty"));
            }

            User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            currentUser.setCurrency(currency.trim().toUpperCase());
            userRepository.save(currentUser);

            return ResponseEntity.ok(Map.of("currency", currentUser.getCurrency()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update currency"));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null ||
                    currentPassword.isEmpty() || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "All password fields are required"));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters"));
            }

            User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
            }

            // Update password
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(currentUser);

            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update password"));
        }
    }
}
