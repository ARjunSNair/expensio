package com.expensetracker.service;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.model.ConfirmationToken;
import com.expensetracker.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConfirmationTokenRepository tokenRepository;

    public void registerUser(String email, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .status(User.Status.PENDING)
                .build();
        userRepository.save(user);
        // In real code, generate a real token
        String token = "dummy-token";
        emailService.sendConfirmationEmail(email, token);
    }

    public boolean confirmUser(String token) {
        Optional<ConfirmationToken> confirmationTokenOpt = tokenRepository.findByToken(token);
        if (confirmationTokenOpt.isEmpty()) {
            return false;
        }
        ConfirmationToken confirmationToken = confirmationTokenOpt.get();
        User user = confirmationToken.getUser();
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
        return true;
    }
} 