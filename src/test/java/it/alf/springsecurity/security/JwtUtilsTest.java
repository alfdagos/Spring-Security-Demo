package it.alf.springsecurity.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    @Test
    void shouldGenerateAndValidateToken() {
        String secret = "01234567890123456789012345678901"; // 32 chars
        JwtUtils utils = new JwtUtils(secret, 1000 * 60);
        String token = utils.generateAccessToken("user1", List.of("ROLE_USER"));
        assertNotNull(token);
        assertTrue(utils.validate(token));
        assertEquals("user1", utils.getUsernameFromToken(token));
        assertTrue(utils.getRolesFromToken(token).contains("ROLE_USER"));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        String secret = "01234567890123456789012345678901";
        JwtUtils utils = new JwtUtils(secret, 1000 * 60);
        assertFalse(utils.validate("this-is-not-a-jwt"));
    }
}
