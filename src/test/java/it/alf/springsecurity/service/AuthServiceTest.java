package it.alf.springsecurity.service;

import it.alf.springsecurity.domain.User;
import it.alf.springsecurity.dto.AuthResponse;
import it.alf.springsecurity.dto.LoginRequest;
import it.alf.springsecurity.dto.RegisterRequest;
import it.alf.springsecurity.repository.RefreshTokenRepository;
import it.alf.springsecurity.repository.RoleRepository;
import it.alf.springsecurity.repository.UserRepository;
import it.alf.springsecurity.security.JwtUtils;
import it.alf.springsecurity.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    JwtUtils jwtUtils;
    AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtils = new JwtUtils("01234567890123456789012345678901", 1000 * 60 * 15);
        authService = new AuthServiceImpl(userRepository, roleRepository, refreshTokenRepository, passwordEncoder, jwtUtils);
    }

    @Test
    void shouldRegisterUser() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("u1");
        req.setEmail("u1@example.com");
        req.setPassword("P@ssw0rd");

        when(userRepository.existsByUsername("u1")).thenReturn(false);
        when(userRepository.existsByEmail("u1@example.com")).thenReturn(false);
        // mock default role lookup used during registration
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new it.alf.springsecurity.domain.Role("ROLE_USER")));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse resp = authService.register(req);
        assertNotNull(resp.getAccessToken());
        assertNotNull(resp.getRefreshToken());
    }

    @Test
    void shouldLoginWithValidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("u1");
        req.setPassword("P@ssw0rd");
        User user = new User();
        user.setUsername("u1");
        user.setPassword("encoded");

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("P@ssw0rd","encoded")).thenReturn(true);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse resp = authService.login(req);
        assertNotNull(resp.getAccessToken());
        assertNotNull(resp.getRefreshToken());
    }

    @Test
    void shouldRefreshWhenValid() {
        // prepare refresh token entity
        var rt = new it.alf.springsecurity.domain.RefreshToken();
        rt.setToken("rt-1");
        var user = new it.alf.springsecurity.domain.User();
        user.setUsername("u1");
        rt.setUser(user);
        rt.setExpiryDate(java.time.Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken("rt-1")).thenReturn(Optional.of(rt));

        var resp = authService.refresh("rt-1");
        assertNotNull(resp.getAccessToken());
        assertEquals("rt-1", resp.getRefreshToken());
    }

    @Test
    void shouldLogoutAndRevokeRefreshToken() {
        var rt = new it.alf.springsecurity.domain.RefreshToken();
        rt.setToken("rt-2");
        rt.setExpiryDate(java.time.Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByToken("rt-2")).thenReturn(Optional.of(rt));

        authService.logout("rt-2");

        assertTrue(rt.isRevoked());
        verify(refreshTokenRepository, times(1)).save(rt);
    }
}
