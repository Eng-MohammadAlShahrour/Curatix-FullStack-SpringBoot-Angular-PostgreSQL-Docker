package com.curatix.museumloan.museum;

import java.time.Instant;

public record MuseumDto(Long id, String name, String country, String city, Instant createdAt) {
    public static MuseumDto from(Museum m) {
        return new MuseumDto(m.getId(), m.getName(), m.getCountry(), m.getCity(), m.getCreatedAt());
    }
}