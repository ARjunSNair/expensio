package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=mydevsupersecretkeymydevsupersecretkey123456")
class ExpenseControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ExpenseService expenseService;
    @Autowired
    private ObjectMapper objectMapper;

    private String jwt;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").status(User.Status.ACTIVE).build();
        // Generate a real JWT for test@example.com, userId=1
        String secret = "mydevsupersecretkeymydevsupersecretkey123456";
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        jwt = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void createExpense_shouldReturnCreated() throws Exception {
        Expense expense = Expense.builder()
                .amount(new BigDecimal("100.00"))
                .description("Lunch")
                .category("Food")
                .date(LocalDate.now())
                .build();
        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated());
    }

    @Test
    void getExpenses_shouldReturnList() throws Exception {
        List<Expense> expenses = List.of(
                Expense.builder().amount(new BigDecimal("50.00")).description("Coffee").category("Food").date(LocalDate.now()).build()
        );
        Mockito.when(expenseService.getExpensesByUserId(1L)).thenReturn(expenses);
        mockMvc.perform(get("/api/expenses")
                .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Coffee"));
    }

    @Test
    void updateExpense_shouldReturnOk() throws Exception {
        Expense updated = Expense.builder()
                .amount(new BigDecimal("120.00"))
                .description("Dinner")
                .category("Food")
                .date(LocalDate.now())
                .build();
        Mockito.when(expenseService.updateExpense(Mockito.eq(1L), Mockito.any(Expense.class), Mockito.eq(1L))).thenReturn(true);
        mockMvc.perform(put("/api/expenses/1")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk());
    }

    @Test
    void updateExpense_shouldReturnNotFoundIfNotOwned() throws Exception {
        Expense updated = Expense.builder().amount(new BigDecimal("120.00")).build();
        Mockito.when(expenseService.updateExpense(Mockito.eq(1L), Mockito.any(Expense.class), Mockito.eq(1L))).thenReturn(false);
        mockMvc.perform(put("/api/expenses/1")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpense_shouldReturnOk() throws Exception {
        Mockito.when(expenseService.deleteExpense(1L, 1L)).thenReturn(true);
        mockMvc.perform(delete("/api/expenses/1")
                .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
    }

    @Test
    void deleteExpense_shouldReturnNotFoundIfNotOwned() throws Exception {
        Mockito.when(expenseService.deleteExpense(1L, 1L)).thenReturn(false);
        mockMvc.perform(delete("/api/expenses/1")
                .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNotFound());
    }
} 