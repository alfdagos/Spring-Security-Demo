package it.alf.springsecurity.web.controller;

import it.alf.springsecurity.dto.UserDto;
import it.alf.springsecurity.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        String username = authentication.getName();
        var user = userService.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(userService.toDto(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDto>> list(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }
}
