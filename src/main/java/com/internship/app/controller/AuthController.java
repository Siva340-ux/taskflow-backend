package com.internship.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.internship.app.repository.UserRepository;
import com.internship.app.model.User;
import com.internship.app.dto.*;
import com.internship.app.config.JwtService;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://taskflow-frontend-ohey.vercel.app", "http://localhost:3000"})
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AuthRequest request) {
        try {
            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail().trim().toLowerCase())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            userRepository.save(user);
            String token = jwtService.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Signup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email is required"));
            }

            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Password is required"));
            }

            User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Invalid credentials"));
            }

            String token = jwtService.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
