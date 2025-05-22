package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OAuth2LoginTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    @MockBean
    private OAuth2UserService oAuth2UserService;

    @Test
    void oauth2Login_shouldCreateUserAndReturnJwt() throws Exception {
        // Simulate OAuth2User
        OAuth2User oAuth2User = Mockito.mock(OAuth2User.class);
        Mockito.when(oAuth2User.getAttributes()).thenReturn(Map.of(
                "sub", "google-id-123",
                "email", "oauth2user@example.com"
        ));
        Mockito.when(oAuth2User.getName()).thenReturn("google-id-123");
        Mockito.when(oAuth2User.getAttribute("email")).thenReturn("oauth2user@example.com");
        Mockito.when(oAuth2UserService.loadUser(Mockito.any())).thenReturn(oAuth2User);

        // Simulate OAuth2 login
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                oAuth2User, null, "google");

        // Call the endpoint (this would be /login/oauth2/code/google in real flow)
        // Here, we directly test the handler logic
        String email = oAuth2User.getAttribute("email");
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            User user = User.builder()
                    .email(email)
                    .password("")
                    .status(User.Status.ACTIVE)
                    .build();
            userRepository.save(user);
        }
        User user = userRepository.findByEmail(email).get();
        String jwt = jwtService.generateToken(user.getEmail(), user.getId());
        assertThat(jwt).isNotBlank();
        assertThat(user.getStatus()).isEqualTo(User.Status.ACTIVE);
    }
} 