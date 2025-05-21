package com.expensetracker.controller;

import com.expensetracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_shouldReturnOkAndCallService() throws Exception {
        String email = "test@example.com";
        String password = "password123";
        var request = new UserRegistrationRequest(email, password);
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(userService).registerUser(email, password);
    }

    @Test
    void confirm_shouldReturnOkIfTokenValid() throws Exception {
        String token = "valid-token";
        Mockito.when(userService.confirmUser(token)).thenReturn(true);
        mockMvc.perform(get("/api/auth/confirm").param("token", token))
                .andExpect(status().isOk());
    }

    @Test
    void confirm_shouldReturnBadRequestIfTokenInvalid() throws Exception {
        String token = "invalid-token";
        Mockito.when(userService.confirmUser(token)).thenReturn(false);
        mockMvc.perform(get("/api/auth/confirm").param("token", token))
                .andExpect(status().isBadRequest());
    }

    static class UserRegistrationRequest {
        public String email;
        public String password;
        public UserRegistrationRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
} 