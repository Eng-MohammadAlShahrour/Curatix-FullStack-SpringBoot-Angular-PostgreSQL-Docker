package com.curatix.museumloan.artifact;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArtifactRepository extends JpaRepository<Artifact, Long> {
    List<Artifact> findByMuseumId(Long museumId);
}