package it.alf.springsecurity.service.impl;

import it.alf.springsecurity.domain.Role;
import it.alf.springsecurity.domain.User;
import it.alf.springsecurity.dto.UserDto;
import it.alf.springsecurity.mapper.UserMapper;
import it.alf.springsecurity.repository.RoleRepository;
import it.alf.springsecurity.repository.UserRepository;
import it.alf.springsecurity.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User createUser(User user) {
        // default role assignment
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDto toDto(User user) {
        return userMapper.toDto(user);
    }

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }
}
