package com.curatix.museumloan.auth;

import com.curatix.museumloan.auth.dto.*;
import com.curatix.museumloan.museum.MuseumRepository;
import com.curatix.museumloan.security.JwtService;
import com.curatix.museumloan.user.User;
import com.curatix.museumloan.user.UserRepository;
import com.curatix.museumloan.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MuseumRepository museumRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (req.role() == UserRole.MUSEUM) {
            if (req.museumId() == null) {
                throw new IllegalArgumentException("museumId is required for MUSEUM role");
            }
            if (!museumRepository.existsById(req.museumId())) {
                throw new IllegalArgumentException("Museum not found: " + req.museumId());
            }
        }
        User u = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .role(req.role())
                .museumId(req.museumId())
                .build();
        userRepository.save(u);
        String token = jwtService.generateToken(u.getId(), u.getEmail(), u.getRole().name());
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getFullName(), u.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        User u = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtService.generateToken(u.getId(), u.getEmail(), u.getRole().name());
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getFullName(), u.getRole().name());
    }
}