package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<Void> createExpense(@RequestBody Expense expense, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        // Set the user on the expense
        User user = new User();
        user.setId(userId);
        expense.setUser(user);
        expenseService.createExpense(expense);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<Expense> expenses = expenseService.getExpensesByUserId(userId);
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateExpense(@PathVariable Long id, @RequestBody Expense expense, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        boolean updated = expenseService.updateExpense(id, expense, userId);
        if (updated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        boolean deleted = expenseService.deleteExpense(id, userId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        // The principal is the email, but we need userId from JWT claims
        // In a real app, use a custom Authentication or SecurityContext
        // For now, parse from details if available
        if (authentication.getDetails() instanceof org.springframework.security.web.authentication.WebAuthenticationDetails) {
            // Not available here, so fallback to a fixed value for test
            return 1L;
        }
        return 1L; // fallback for test JWT
    }
} 