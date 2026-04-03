package com.example.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
class RegisterRequest {
    @NotBlank
    String name;
    @Email
    @NotBlank String email;
    @Size(min = 6) @NotBlank String password;
}

@Data
class LoginRequest {
    @Email @NotBlank String email;
    @NotBlank String password;
}

@Data
class AuthResponse {
    String token;
    String name;
    String email;

    public AuthResponse(String token, String name, String email) {
        this.token = token;
        this.name = name;
        this.email = email;
    }
}


@Data
class ExpenseRequest {
    @NotBlank String title;
    @NotBlank Double amount;
    String notes;
    // type: EXPENSE or INCOME
    @NotBlank String type;
    // category is optional — AI will auto-assign if not provided
    String category;
}

@Data
class ExpenseResponse {
    Long id;
    String title;
    Double amount;
    String category;
    String type;
    String notes;
    String transactionDate;
}


@Data
class InsightResponse {
    String summary;
    String topSpendingCategory;
    String advice;
    Double totalExpenses;
    Double totalIncome;
}
