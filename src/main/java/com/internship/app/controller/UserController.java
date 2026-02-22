package com.internship.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.internship.app.model.User;
import com.internship.app.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // ✅ Get Logged-in User Profile
    @GetMapping("/me")
    public User getProfile(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ Update Profile
    @PutMapping("/me")
    public User updateProfile(
            @RequestBody User updatedUser,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        user.setName(updatedUser.getName());

        return userRepository.save(user);
    }
}