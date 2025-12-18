package com.lostfound.backend.controller;

import com.lostfound.backend.entity.User;
import com.lostfound.backend.exception.NotFoundException;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.repository.UserRepository;
import com.lostfound.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminUserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<User>> list(HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        List<User> users = userRepository.findAll()
                .stream()
                .map(this::sanitizeUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("Username and password are required");
        }
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("USER");
        }
        user.setRole(user.getRole().toUpperCase());
        userRepository.findByUsername(user.getUsername()).ifPresent(existing -> {
            throw new IllegalArgumentException("Username already exists");
        });
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitizeUser(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private User sanitizeUser(User source) {
        User sanitized = new User();
        sanitized.setId(source.getId());
        sanitized.setUsername(source.getUsername());
        sanitized.setRole(source.getRole());
        return sanitized;
    }
}
