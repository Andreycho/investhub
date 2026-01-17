package com.example.investhub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private String base64Secret;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        byte[] rawKey = "this_is_a_test_secret_key_with_32+_bytes!".getBytes(StandardCharsets.UTF_8);
        base64Secret = Base64.getEncoder().encodeToString(rawKey);

        ReflectionTestUtils.setField(jwtService, "secretKey", base64Secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);

        userDetails = User.withUsername("test1")
                .password("x")
                .authorities("USER")
                .build();
    }

    @Test
    void generateToken_shouldContainUsernameAsSubject() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("test1", username);
    }

    @Test
    void isTokenValid_shouldReturnTrue_forCorrectUser_andNotExpired() {
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalse_forDifferentUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetails other = User.withUsername("otherUser")
                .password("x")
                .authorities("USER")
                .build();

        assertFalse(jwtService.isTokenValid(token, other));
    }

    @Test
    void tokenShouldExpire_basedOnExpirationTime() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String token = jwtService.generateToken(userDetails);

        Thread.sleep(10);

        assertThrows(Exception.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void generateToken_withExtraClaims_shouldStoreClaims() {
        String token = jwtService.generateToken(Map.of("role", "USER", "x", 123), userDetails);

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        Integer x = jwtService.extractClaim(token, claims -> claims.get("x", Integer.class));

        assertEquals("USER", role);
        assertEquals(123, x);
    }

    @Test
    void extractClaim_shouldReturnExpirationDate() {
        String token = jwtService.generateToken(userDetails);

        Date exp = jwtService.extractClaim(token, claims -> claims.getExpiration());

        assertNotNull(exp);
        assertTrue(exp.after(new Date()));
    }

    @Test
    void tokenWithWrongSecret_shouldFailParsing() {
        String token = jwtService.generateToken(userDetails);

        JwtService otherJwtService = new JwtService();
        byte[] otherRawKey = "another_test_secret_key_with_32+_bytes!".getBytes(StandardCharsets.UTF_8);
        String otherBase64 = Base64.getEncoder().encodeToString(otherRawKey);

        ReflectionTestUtils.setField(otherJwtService, "secretKey", otherBase64);
        ReflectionTestUtils.setField(otherJwtService, "jwtExpiration", 60_000L);

        assertThrows(Exception.class, () -> otherJwtService.extractUsername(token));
    }
}