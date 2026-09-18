package de.co2management.backend.repository;

import de.co2management.backend.entity.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BenutzerRepository extends JpaRepository<Benutzer, Long> {
    Optional<Benutzer> findByUsername(String username);
    List<Benutzer> findByStandortId(Long standortId); 
}

