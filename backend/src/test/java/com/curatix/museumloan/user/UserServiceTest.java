package com.curatix.museumloan.user;

import com.curatix.museumloan.museum.Museum;
import com.curatix.museumloan.museum.MuseumRepository;
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
class UserServiceTest {

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired MuseumRepository museumRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void create_persistsUser() {
        var dto = userService.create(new UserCreateRequest("user@test.com", "password123", "Full Name", UserRole.REVIEWER, null));
        assertNotNull(dto.id());
        assertEquals("user@test.com", dto.email());
        assertEquals(UserRole.REVIEWER, dto.role());
    }

    @Test
    void create_hashesPassword() {
        userService.create(new UserCreateRequest("hash@test.com", "password123", "H", UserRole.REVIEWER, null));
        var u = userRepository.findByEmail("hash@test.com").orElseThrow();
        assertTrue(passwordEncoder.matches("password123", u.getPasswordHash()));
    }

    @Test
    void create_throwsOnDuplicateEmail() {
        userService.create(new UserCreateRequest("dup@test.com", "password123", "A", UserRole.REVIEWER, null));
        assertThrows(IllegalArgumentException.class, () ->
            userService.create(new UserCreateRequest("dup@test.com", "password456", "B", UserRole.ADMIN, null))
        );
    }

    @Test
    void create_requiresMuseumForMuseumRole() {
        assertThrows(IllegalArgumentException.class, () ->
            userService.create(new UserCreateRequest("m@test.com", "password123", "M", UserRole.MUSEUM, null))
        );
    }

    @Test
    void create_throwsOnInvalidMuseumId() {
        assertThrows(IllegalArgumentException.class, () ->
            userService.create(new UserCreateRequest("m@test.com", "password123", "M", UserRole.MUSEUM, 99999L))
        );
    }

    @Test
    void delete_removesUser() {
        var dto = userService.create(new UserCreateRequest("del@test.com", "password123", "D", UserRole.REVIEWER, null));
        userService.delete(dto.id());
        assertFalse(userRepository.existsById(dto.id()));
    }

    @Test
    void listAll_returnsAllUsers() {
        userService.create(new UserCreateRequest("u1@test.com", "password123", "U1", UserRole.REVIEWER, null));
        userService.create(new UserCreateRequest("u2@test.com", "password123", "U2", UserRole.ADMIN, null));
        assertEquals(2, userService.listAll().size());
    }
}