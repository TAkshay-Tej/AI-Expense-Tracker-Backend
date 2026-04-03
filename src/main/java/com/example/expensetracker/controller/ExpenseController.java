package com.example.expensetracker.controller;


import com.example.expensetracker.dto.ExpenseDto;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto.Response> addExpense(
            @Valid @RequestBody ExpenseDto.Request request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.addExpense(request, user));
    }


    @GetMapping
    public ResponseEntity<List<ExpenseDto.Response>> getExpenses(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.getAllExpenses(user));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        expenseService.deleteExpense(id, user);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/insights")
    public ResponseEntity<ExpenseDto.InsightResponse> getInsights(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.getInsights(user));
    }
}
