package com.curatix.museumloan.artifact;

import com.curatix.museumloan.museum.Museum;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "artifacts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Artifact {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "museum_id", nullable = false)
    private Museum museum;

    @Column(nullable = false)
    private String name;

    private String era;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); if (this.status == null) this.status = "AVAILABLE"; }
}