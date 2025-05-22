package com.expensetracker.service;

import com.expensetracker.model.User;
import com.expensetracker.model.ConfirmationToken;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_shouldCreateUserWithPendingStatusAndSendConfirmationEmail() {
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.registerUser(email, password);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedUser.getStatus()).isEqualTo(User.Status.PENDING);
        verify(emailService).sendConfirmationEmail(eq(email), anyString());
    }

    @Test
    void confirmUser_withValidToken_shouldActivateUser() {
        // Arrange
        String token = "valid-token";
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .status(User.Status.PENDING)
                .build();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, user);
        ConfirmationTokenRepository tokenRepository = mock(ConfirmationTokenRepository.class);
        when(tokenRepository.findByToken(token)).thenReturn(java.util.Optional.of(confirmationToken));
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        UserService userService = new UserService(userRepository, emailService, passwordEncoder, tokenRepository, jwtService);

        // Act
        boolean result = userService.confirmUser(token);

        // Assert
        assertThat(result).isTrue();
        assertThat(user.getStatus()).isEqualTo(User.Status.ACTIVE);
    }

    @Test
    void confirmUser_withInvalidToken_shouldFail() {
        // Arrange
        String token = "invalid-token";
        ConfirmationTokenRepository tokenRepository = mock(ConfirmationTokenRepository.class);
        when(tokenRepository.findByToken(token)).thenReturn(java.util.Optional.empty());
        UserService userService = new UserService(userRepository, emailService, passwordEncoder, tokenRepository, jwtService);

        // Act
        boolean result = userService.confirmUser(token);

        // Assert
        assertThat(result).isFalse();
    }
} 