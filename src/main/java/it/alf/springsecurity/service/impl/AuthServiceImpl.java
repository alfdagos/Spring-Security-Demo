package it.alf.springsecurity.service.impl;

import it.alf.springsecurity.domain.RefreshToken;
import it.alf.springsecurity.domain.User;
import it.alf.springsecurity.dto.AuthResponse;
import it.alf.springsecurity.dto.LoginRequest;
import it.alf.springsecurity.dto.RegisterRequest;
import it.alf.springsecurity.repository.RefreshTokenRepository;
import it.alf.springsecurity.repository.RoleRepository;
import it.alf.springsecurity.repository.UserRepository;
import it.alf.springsecurity.security.JwtUtils;
import it.alf.springsecurity.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenDurationMs;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already used");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // assign default role if present in DB
        var defaultRole = roleRepository.findByName("ROLE_USER");
        if (defaultRole.isPresent()) {
            user.getRoles().add(defaultRole.get());
        } else {
            throw new IllegalStateException("Default role ROLE_USER not found in database");
        }
        userRepository.save(user);

        // Simulated email (log)
        System.out.println("[SIMULATED EMAIL] Welcome " + user.getUsername());

        // Return tokens by logging user in automatically
        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), List.of("ROLE_USER"));
        RefreshToken rt = createRefreshToken(user);
        return new AuthResponse(accessToken, rt.getToken());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        List<String> roles = user.getRoles().stream().map(r -> r.getName()).toList();
        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), roles);
        RefreshToken rt = createRefreshToken(user);
        return new AuthResponse(accessToken, rt.getToken());
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshTokenRepository.save(rt);
        return rt;
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        if (rt.isRevoked() || rt.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }
        User user = rt.getUser();
        List<String> roles = user.getRoles().stream().map(r -> r.getName()).toList();
        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), roles);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken).orElseThrow();
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
    }
}
