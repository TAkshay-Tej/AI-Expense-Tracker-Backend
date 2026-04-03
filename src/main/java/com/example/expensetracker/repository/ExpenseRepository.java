package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Expense> findByUserIdAndTypeOrderByTransactionDateDesc(Long userId, String type);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
            "AND e.transactionDate BETWEEN :start AND :end ORDER BY e.transactionDate DESC")
    List<Expense> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.type = :type")
    Double sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
}
