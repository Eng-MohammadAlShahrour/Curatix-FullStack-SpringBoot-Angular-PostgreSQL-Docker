package com.curatix.museumloan.auth;

import com.curatix.museumloan.auth.dto.LoginRequest;
import com.curatix.museumloan.auth.dto.RegisterRequest;
import com.curatix.museumloan.user.UserRepository;
import com.curatix.museumloan.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void register_createsUserAndReturnsToken() {
        var req = new RegisterRequest("new@test.com", "password123", "New User", UserRole.REVIEWER, null);
        var resp = authService.register(req);

        assertNotNull(resp.token());
        assertEquals("new@test.com", resp.email());
        assertEquals("REVIEWER", resp.role());
        assertTrue(userRepository.existsByEmail("new@test.com"));
    }

    @Test
    void register_hashesPasswordWithBcrypt() {
        authService.register(new RegisterRequest("hash@test.com", "secret123", "Hash User", UserRole.REVIEWER, null));
        var user = userRepository.findByEmail("hash@test.com").orElseThrow();

        assertNotEquals("secret123", user.getPasswordHash());
        assertTrue(passwordEncoder.matches("secret123", user.getPasswordHash()));
    }

    @Test
    void register_throwsOnDuplicateEmail() {
        authService.register(new RegisterRequest("dup@test.com", "password123", "First", UserRole.REVIEWER, null));

        assertThrows(IllegalArgumentException.class, () ->
            authService.register(new RegisterRequest("dup@test.com", "password456", "Second", UserRole.ADMIN, null))
        );
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        authService.register(new RegisterRequest("login@test.com", "password123", "Login User", UserRole.REVIEWER, null));
        var resp = authService.login(new LoginRequest("login@test.com", "password123"));

        assertNotNull(resp.token());
        assertEquals("login@test.com", resp.email());
    }

    @Test
    void login_throwsOnWrongPassword() {
        authService.register(new RegisterRequest("wrong@test.com", "password123", "Wrong", UserRole.REVIEWER, null));

        assertThrows(IllegalArgumentException.class, () ->
            authService.login(new LoginRequest("wrong@test.com", "wrongpass"))
        );
    }

    @Test
    void login_throwsOnUnknownEmail() {
        assertThrows(IllegalArgumentException.class, () ->
            authService.login(new LoginRequest("unknown@test.com", "password123"))
        );
    }
}