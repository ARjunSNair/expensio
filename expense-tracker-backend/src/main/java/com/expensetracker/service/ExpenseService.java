package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public void createExpense(Expense expense) {
        expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByUserId(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    public boolean updateExpense(Long expenseId, Expense updated, Long userId) {
        Optional<Expense> opt = expenseRepository.findById(expenseId);
        if (opt.isEmpty()) return false;
        Expense existing = opt.get();
        if (!existing.getUser().getId().equals(userId)) return false;
        existing.setAmount(updated.getAmount());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setDate(updated.getDate());
        expenseRepository.save(existing);
        return true;
    }

    public boolean deleteExpense(Long expenseId, Long userId) {
        Optional<Expense> opt = expenseRepository.findById(expenseId);
        if (opt.isEmpty()) return false;
        Expense existing = opt.get();
        if (!existing.getUser().getId().equals(userId)) return false;
        expenseRepository.delete(existing);
        return true;
    }
} 