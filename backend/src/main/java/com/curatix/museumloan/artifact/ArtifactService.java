package com.curatix.museumloan.artifact;

import com.curatix.museumloan.museum.Museum;
import com.curatix.museumloan.museum.MuseumRepository;
import com.curatix.museumloan.user.User;
import com.curatix.museumloan.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final MuseumRepository museumRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ArtifactDto> listAll() {
        return artifactRepository.findAll().stream().map(ArtifactDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ArtifactDto> listByMuseum(Long museumId) {
        return artifactRepository.findByMuseumId(museumId).stream().map(ArtifactDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ArtifactDto get(Long id) {
        Artifact a = artifactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artifact not found: " + id));
        return ArtifactDto.from(a);
    }

    @Transactional
    public ArtifactDto create(ArtifactCreateRequest req, Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (u.getMuseumId() == null) {
            throw new IllegalArgumentException("User has no museum assigned");
        }
        Museum m = museumRepository.findById(u.getMuseumId())
                .orElseThrow(() -> new IllegalArgumentException("Museum not found"));
        Artifact a = Artifact.builder()
                .museum(m)
                .name(req.name())
                .era(req.era())
                .description(req.description())
                .status("AVAILABLE")
                .build();
        artifactRepository.save(a);
        return ArtifactDto.from(a);
    }
}