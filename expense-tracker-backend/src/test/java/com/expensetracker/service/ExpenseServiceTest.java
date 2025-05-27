package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;
    @InjectMocks
    private ExpenseService expenseService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).email("test@example.com").status(User.Status.ACTIVE).build();
    }

    @Test
    void createExpense_shouldSaveExpense() {
        Expense expense = Expense.builder()
                .user(user)
                .amount(new BigDecimal("100.00"))
                .description("Lunch")
                .category("Food")
                .date(LocalDate.now())
                .build();
        expenseService.createExpense(expense);
        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getAmount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void getExpensesByUserId_shouldReturnExpenses() {
        when(expenseRepository.findByUserId(1L)).thenReturn(List.of(
                Expense.builder().user(user).amount(new BigDecimal("50.00")).description("Coffee").category("Food").date(LocalDate.now()).build()
        ));
        List<Expense> expenses = expenseService.getExpensesByUserId(1L);
        assertThat(expenses).hasSize(1);
        assertThat(expenses.get(0).getDescription()).isEqualTo("Coffee");
    }
} 