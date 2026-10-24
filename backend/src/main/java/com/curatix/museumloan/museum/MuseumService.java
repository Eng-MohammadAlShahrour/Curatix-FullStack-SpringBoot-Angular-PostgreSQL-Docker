package com.curatix.museumloan.museum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MuseumService {

    private final MuseumRepository museumRepository;

    public List<MuseumDto> listAll() {
        return museumRepository.findAll().stream().map(MuseumDto::from).toList();
    }

    public MuseumDto get(Long id) {
        Museum m = museumRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Museum not found: " + id));
        return MuseumDto.from(m);
    }

    @Transactional
    public MuseumDto create(MuseumCreateRequest req) {
        Museum m = Museum.builder()
                .name(req.name())
                .country(req.country())
                .city(req.city())
                .build();
        museumRepository.save(m);
        return MuseumDto.from(m);
    }
}