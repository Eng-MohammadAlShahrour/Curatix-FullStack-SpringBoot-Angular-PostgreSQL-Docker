package com.curatix.museumloan.museum;

import com.curatix.museumloan.audit.AuditableEntityListener;

import jakarta.persistence.*;
import jakarta.persistence.EntityListeners;
import lombok.*;
import java.time.Instant;

@Entity
@EntityListeners(AuditableEntityListener.class)
@Table(name = "museums")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Museum {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    private String city;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); }
}