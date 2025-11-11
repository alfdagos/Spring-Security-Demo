package it.alf.springsecurity.service;

import it.alf.springsecurity.domain.Role;
import it.alf.springsecurity.domain.User;
import it.alf.springsecurity.mapper.UserMapper;
import it.alf.springsecurity.repository.RoleRepository;
import it.alf.springsecurity.repository.UserRepository;
import it.alf.springsecurity.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    UserMapper userMapper;

    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, roleRepository, userMapper);
    }

    @Test
    void shouldFindByUsername() {
        User u = new User();
        u.setUsername("u1");
        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(u));
        var opt = userService.findByUsername("u1");
        assertTrue(opt.isPresent());
        assertEquals("u1", opt.get().getUsername());
    }

    @Test
    void shouldReturnPagedUsers() {
        User u = new User(); u.setUsername("u1");
        when(userRepository.findAll(PageRequest.of(0,10))).thenReturn(new PageImpl<>(List.of(u)));
        when(userMapper.toDto(u)).thenReturn(null);
        var page = userService.findAll(PageRequest.of(0,10));
        assertEquals(1, page.getTotalElements());
    }
}
