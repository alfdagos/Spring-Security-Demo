package it.alf.springsecurity.security;

import it.alf.springsecurity.domain.Role;
import it.alf.springsecurity.domain.User;
import it.alf.springsecurity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Mock
    UserRepository userRepository;

    CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadUserByUsername() {
        User u = new User();
        u.setUsername("user1");
        u.setPassword("pwd");
        Role r = new Role("ROLE_USER");
        u.getRoles().add(r);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(u));

        var ud = service.loadUserByUsername("user1");
        assertEquals("user1", ud.getUsername());
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
