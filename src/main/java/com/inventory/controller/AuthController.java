package com.inventory.controller;

import com.inventory.dto.DTOs.*;
import com.inventory.model.*;
import com.inventory.repository.UserRepository;
import com.inventory.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
            String token = jwtUtils.generateToken(auth);
            UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
            return ResponseEntity.ok(new JwtResponse(token, ud.getId(), ud.getName(), ud.getUsername(), ud.getRole()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid email or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            return ResponseEntity.badRequest().body(ApiResponse.error("Email already in use"));

        User user = User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .role(Role.USER)
            .build();
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful! Please login."));
    }
}
