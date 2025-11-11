package it.alf.springsecurity.service;

import it.alf.springsecurity.domain.User;
import it.alf.springsecurity.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> findByUsername(String username);
    UserDto toDto(User user);
    Page<UserDto> findAll(Pageable pageable);
}
