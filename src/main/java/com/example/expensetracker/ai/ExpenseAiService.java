package com.example.expensetracker.ai;


import com.example.expensetracker.entity.Expense;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseAiService {
    private final ChatClient.Builder chatClientBuilder;


    public String categorizeExpense(String title, String notes, String type) {
        // Income doesn't need AI categorization
        if ("INCOME".equalsIgnoreCase(type)) {
            return "Salary";
        }

        String prompt = """
                You are an expense categorization assistant.
                Given the following expense details, return ONLY the category name — nothing else.
                
                Choose from: Food, Transport, Shopping, Health, Entertainment, Utilities, Education, Travel, Other
                
                Expense title: "%s"
                Notes: "%s"
                
                Category:
                """.formatted(title, notes != null ? notes : "");

        try {
            ChatClient client = chatClientBuilder.build();
            String category = client.prompt(prompt).call().content().trim();
            log.info("AI categorized '{}' as '{}'", title, category);
            return category;
        } catch (Exception e) {
            log.error("AI categorization failed, defaulting to 'Other': {}", e.getMessage());
            return "Other";
        }
    }


    public SpendingInsight generateInsights(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            return new SpendingInsight(
                    "No expenses recorded yet.",
                    "None",
                    "Start adding your expenses to get personalized insights!",
                    0.0, 0.0
            );
        }

        double totalExpenses = expenses.stream()
                .filter(e -> "EXPENSE".equals(e.getType()))
                .mapToDouble(Expense::getAmount)
                .sum();

        double totalIncome = expenses.stream()
                .filter(e -> "INCOME".equals(e.getType()))
                .mapToDouble(Expense::getAmount)
                .sum();

        // Build expense summary for AI
        String expenseSummary = expenses.stream()
                .filter(e -> "EXPENSE".equals(e.getType()))
                .map(e -> "- %s: $%.2f (%s)".formatted(e.getTitle(), e.getAmount(), e.getCategory()))
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a personal finance advisor analyzing a user's spending habits.
                
                Here is their recent expense data:
                %s
                
                Total Expenses: $%.2f
                Total Income: $%.2f
                
                Please provide:
                1. A brief 2-sentence summary of their spending patterns.
                2. Their top spending category.
                3. One specific, actionable piece of advice to improve their finances.
                
                Respond in this exact format:
                SUMMARY: <summary here>
                TOP_CATEGORY: <category name>
                ADVICE: <advice here>
                """.formatted(expenseSummary, totalExpenses, totalIncome);

        try {
            ChatClient client = chatClientBuilder.build();
            String response = client.prompt(prompt).call().content().trim();
            return parseInsightResponse(response, totalExpenses, totalIncome);
        } catch (Exception e) {
            log.error("AI insight generation failed: {}", e.getMessage());
            return new SpendingInsight(
                    "Unable to generate insights at this time.",
                    "Unknown",
                    "Please try again later.",
                    totalExpenses, totalIncome
            );
        }
    }

    private SpendingInsight parseInsightResponse(String response, double totalExpenses, double totalIncome) {
        String summary = extractField(response, "SUMMARY:");
        String topCategory = extractField(response, "TOP_CATEGORY:");
        String advice = extractField(response, "ADVICE:");
        return new SpendingInsight(summary, topCategory, advice, totalExpenses, totalIncome);
    }

    private String extractField(String response, String field) {
        for (String line : response.split("\n")) {
            if (line.startsWith(field)) {
                return line.substring(field.length()).trim();
            }
        }
        return "";
    }

    public record SpendingInsight(
            String summary,
            String topSpendingCategory,
            String advice,
            double totalExpenses,
            double totalIncome
    ) {}
}
