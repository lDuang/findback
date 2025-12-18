package com.lostfound.backend.controller;

import com.lostfound.backend.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import com.lostfound.backend.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> payload = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(payload);
    }
}
