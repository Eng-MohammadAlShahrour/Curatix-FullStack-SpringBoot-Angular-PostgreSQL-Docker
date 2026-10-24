package com.curatix.museumloan.museum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MuseumServiceTest {

    @Autowired MuseumService museumService;

    @Test
    void create_persistsMuseum() {
        var dto = museumService.create(new MuseumCreateRequest("Test Museum", "Germany", "Berlin"));
        assertNotNull(dto.id());
        assertEquals("Test Museum", dto.name());
        assertEquals("Germany", dto.country());
        assertEquals("Berlin", dto.city());
    }

    @Test
    void listAll_returnsAllMuseums() {
        museumService.create(new MuseumCreateRequest("M1", "C1", "City1"));
        museumService.create(new MuseumCreateRequest("M2", "C2", "City2"));
        assertEquals(2, museumService.listAll().size());
    }

    @Test
    void get_returnsMuseumById() {
        var created = museumService.create(new MuseumCreateRequest("Test", "Country", "City"));
        var fetched = museumService.get(created.id());
        assertEquals(created.id(), fetched.id());
        assertEquals("Test", fetched.name());
    }

    @Test
    void get_throwsOnMissingMuseum() {
        assertThrows(IllegalArgumentException.class, () -> museumService.get(99999L));
    }
}