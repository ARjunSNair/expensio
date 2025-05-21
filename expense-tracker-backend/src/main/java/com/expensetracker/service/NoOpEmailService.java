package com.expensetracker.service;

import org.springframework.stereotype.Service;

@Service
public class NoOpEmailService implements EmailService {
    @Override
    public void sendConfirmationEmail(String to, String token) {
        // No-op for tests and local dev
    }
} 