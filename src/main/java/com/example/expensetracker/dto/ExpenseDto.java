package com.example.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ExpenseDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Title is required")
        private String title;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        private Double amount;

        private String notes;

        @NotBlank(message = "Type is required (EXPENSE or INCOME)")
        private String type; // EXPENSE | INCOME

        // Optional: if null, AI will auto-categorize
        private String category;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private Double amount;
        private String category;
        private String type;
        private String notes;
        private String transactionDate;
    }

    @Data
    @AllArgsConstructor
    public static class InsightResponse {
        private String summary;
        private String topSpendingCategory;
        private String advice;
        private Double totalExpenses;
        private Double totalIncome;
    }

}
