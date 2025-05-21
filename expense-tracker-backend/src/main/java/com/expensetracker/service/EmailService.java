package com.expensetracker.service;

public interface EmailService {
    void sendConfirmationEmail(String to, String token);
} 