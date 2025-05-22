package com.expensetracker.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.security.Key;
import java.util.Date;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=mydevsupersecretkeymydevsupersecretkey123456")
class ProtectedControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private String generateJwt(String email) {
        String secret = "mydevsupersecretkeymydevsupersecretkey123456";
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", 1L)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 3600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void hello_withValidJwt_shouldReturnGreeting() throws Exception {
        String jwt = generateJwt("test@example.com");
        mockMvc.perform(MockMvcRequestBuilders.get("/api/protected/hello")
                .header("Authorization", "Bearer " + jwt)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, test@example.com!"));
    }

    @Test
    void hello_withoutJwt_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/protected/hello"))
                .andExpect(status().isUnauthorized());
    }
} 