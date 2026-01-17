package com.example.investhub.controller;

import com.example.investhub.model.User;
import com.example.investhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test: Controller + Security + AuthenticationManager + Repo + H2.
 */

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldCreateUser_withEncodedPassword_andDefaultUsdBalance() throws Exception {
        String body = """
                {
                  "username": "newuser",
                  "password": "pass123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("registered")));

        User u = userRepository.findByUsername("newuser").orElseThrow();
        assertEquals("newuser", u.getUsername());
        assertNotNull(u.getPassword());
        assertTrue(passwordEncoder.matches("pass123", u.getPassword()), "Password must be encoded (BCrypt)");
        assertNotNull(u.getUsdBalance());
        assertEquals(0, u.getUsdBalance().compareTo(new BigDecimal("30000.00")));
    }

    @Test
    void register_whenUsernameExists_shouldReturnConflict() throws Exception {
        // seed existing user
        User u = new User();
        u.setUsername("dup");
        u.setPassword(passwordEncoder.encode("x"));
        u.setUsdBalance(new BigDecimal("30000.00"));
        userRepository.save(u);

        String body = """
                {
                  "username": "dup",
                  "password": "pass"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Username already exists")));
    }

    @Test
    void login_withValidCredentials_shouldReturnJwtAndUserInfo() throws Exception {
        // seed user with encoded password
        User u = new User();
        u.setUsername("loginuser");
        u.setPassword(passwordEncoder.encode("pass123"));
        u.setUsdBalance(new BigDecimal("30000.00"));
        u = userRepository.save(u);

        String body = """
                {
                  "username": "loginuser",
                  "password": "pass123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.expiresIn", greaterThan(0)))
                .andExpect(jsonPath("$.userId", is(u.getId().intValue())))
                .andExpect(jsonPath("$.username", is("loginuser")))
                .andExpect(jsonPath("$.usdBalance", anyOf(is(30000.00), is("30000.00"))));
    }

    @Test
    void login_withWrongPassword_shouldReturnUnauthorized() throws Exception {
        User u = new User();
        u.setUsername("wrongpass");
        u.setPassword(passwordEncoder.encode("right"));
        u.setUsdBalance(new BigDecimal("30000.00"));
        userRepository.save(u);

        String body = """
                {
                  "username": "wrongpass",
                  "password": "wrong"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_whenUserNotFound_shouldReturnUnauthorized() throws Exception {
        String body = """
                {
                  "username": "missing",
                  "password": "pass"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid credentials")));
    }

    @Test
    void getAllUsers_shouldReturnUsersList() throws Exception {
        User u1 = new User();
        u1.setUsername("u1");
        u1.setPassword(passwordEncoder.encode("x"));
        u1.setUsdBalance(new BigDecimal("30000.00"));
        userRepository.save(u1);

        User u2 = new User();
        u2.setUsername("u2");
        u2.setPassword(passwordEncoder.encode("y"));
        u2.setUsdBalance(new BigDecimal("25000.00"));
        userRepository.save(u2);

        mockMvc.perform(get("/api/auth/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("u1", "u2")))
                .andExpect(jsonPath("$[*].id", everyItem(notNullValue())));
    }
}