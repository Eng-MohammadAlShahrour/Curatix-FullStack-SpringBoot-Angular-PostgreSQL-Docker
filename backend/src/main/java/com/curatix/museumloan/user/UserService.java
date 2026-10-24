package com.curatix.museumloan.user;

import com.curatix.museumloan.museum.Museum;
import com.curatix.museumloan.museum.MuseumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MuseumRepository museumRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> listAll() {
        Map<Long, String> museumNames = museumRepository.findAll().stream()
                .collect(Collectors.toMap(Museum::getId, Museum::getName));

        return userRepository.findAll().stream()
                .map(u -> toDto(u, museumNames))
                .toList();
    }

    public UserDto get(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toDto(u, null);
    }

    @Transactional
    public UserDto create(UserCreateRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already exists: " + req.email());
        }

        if (req.role() == UserRole.MUSEUM) {
            if (req.museumId() == null) {
                throw new IllegalArgumentException("museumId is required for MUSEUM role");
            }
            if (!museumRepository.existsById(req.museumId())) {
                throw new IllegalArgumentException("Museum not found: " + req.museumId());
            }
        } else if (req.museumId() != null) {
            throw new IllegalArgumentException("museumId only valid for MUSEUM role");
        }

        User u = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .role(req.role())
                .museumId(req.museumId())
                .build();

        userRepository.save(u);
        return toDto(u, null);
    }

    @Transactional
    public UserDto updateRole(Long id, UserUpdateRoleRequest req) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (req.role() == UserRole.MUSEUM) {
            if (req.museumId() == null) {
                throw new IllegalArgumentException("museumId is required for MUSEUM role");
            }
            if (!museumRepository.existsById(req.museumId())) {
                throw new IllegalArgumentException("Museum not found: " + req.museumId());
            }
        }

        u.setRole(req.role());
        u.setMuseumId(req.role() == UserRole.MUSEUM ? req.museumId() : null);
        userRepository.save(u);
        return toDto(u, null);
    }

    @Transactional
    public void delete(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        userRepository.delete(u);
    }

    private UserDto toDto(User u, Map<Long, String> museumNamesCache) {
        String museumName = null;
        if (u.getMuseumId() != null) {
            if (museumNamesCache != null) {
                museumName = museumNamesCache.get(u.getMuseumId());
            } else {
                museumName = museumRepository.findById(u.getMuseumId())
                        .map(Museum::getName)
                        .orElse(null);
            }
        }
        return new UserDto(
                u.getId(), u.getEmail(), u.getFullName(),
                u.getRole(), u.getMuseumId(), museumName, u.getCreatedAt()
        );
    }
}