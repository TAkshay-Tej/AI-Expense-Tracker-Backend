package com.example.expensetracker.service;


import com.example.expensetracker.ai.ExpenseAiService;
import com.example.expensetracker.dto.ExpenseDto;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseAiService aiService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ExpenseDto.Response addExpense(ExpenseDto.Request request, User user) {
        // Auto-categorize with AI if category not provided
        String category = request.getCategory();
        if (category == null || category.isBlank()) {
            category = aiService.categorizeExpense(
                    request.getTitle(),
                    request.getNotes(),
                    request.getType()
            );
        }

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(category)
                .type(request.getType().toUpperCase())
                .notes(request.getNotes())
                .user(user)
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    public List<ExpenseDto.Response> getAllExpenses(User user) {
        return expenseRepository
                .findByUserIdOrderByTransactionDateDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteExpense(Long expenseId, User user) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        expenseRepository.delete(expense);
    }

    public ExpenseDto.InsightResponse getInsights(User user) {
        List<Expense> expenses = expenseRepository
                .findByUserIdOrderByTransactionDateDesc(user.getId());

        ExpenseAiService.SpendingInsight insight = aiService.generateInsights(expenses);

        return new ExpenseDto.InsightResponse(
                insight.summary(),
                insight.topSpendingCategory(),
                insight.advice(),
                insight.totalExpenses(),
                insight.totalIncome()
        );
    }

    private ExpenseDto.Response toResponse(Expense expense) {
        return new ExpenseDto.Response(
                expense.getId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getType(),
                expense.getNotes(),
                expense.getTransactionDate() != null
                        ? expense.getTransactionDate().format(FORMATTER) : null
        );
    }
}
