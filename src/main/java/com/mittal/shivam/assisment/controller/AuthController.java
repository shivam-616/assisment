package com.mittal.shivam.assisment.controller;

import com.mittal.shivam.assisment.Entities.User;
import com.mittal.shivam.assisment.dto.AuthRequest;
import com.mittal.shivam.assisment.dto.AuthResponse;
import com.mittal.shivam.assisment.repository.UserRepository;
import com.mittal.shivam.assisment.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        // 1. Create and save the user
        User user = User.builder()
                .email(request.email())
                .role(request.role())
                .timezone(request.timezone())
                .build();
        userRepository.save(user);

        // 2. Build claims and generate token
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId().toString());

        String token = jwtService.generateToken(claims, user.getEmail());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .timezone(user.getTimezone())
                .build());
    }
}